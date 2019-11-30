package es.upm.dit.cnvr.zkBank.zk;

import es.upm.dit.cnvr.zkBank.Bank;
import es.upm.dit.cnvr.zkBank.Database;
import es.upm.dit.cnvr.zkBank.model.BankClientI;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

//import java.util.Scanner;

public class ExploreZk implements Watcher{

	private static ZooKeeper zk = null;
	private static String rootMembers = "/members";
	private static String rootTransactions = "/transactions";
	private static String aMember = "/member-";
	private static int nMembers  = 0;
	private static int nTransactions = 0;
	private static List<String> listTransactions = null;
	private static List<String> listMembers  = null;

	final String[] hosts;

	private final String myHostName;
	private Bank bank;
	private final String[] banks;

	private static Integer mutex        = -1;
	private static Integer mutexBarrier = -2;
	private static Integer mutexMember  = -3;
	private static final int SESSION_TIMEOUT = 5000;
	Watcher memberWatcher = new Watcher() {
		public void process(WatchedEvent event) {

			Stat s = null;

			System.out.println("------------------Watcher MEMBER ------------------");
			System.out.println("Member: " + event.getType() + ", " + event.getPath());
			try {
				if (event.getPath().equals(rootMembers)) {
					listMembers = zk.getChildren(rootMembers, this, s);
					System.out.println(listMembers);
					synchronized (mutexMember) {
						nMembers++;

//						Thread.sleep(2000);


						mutexMember.notify();
					}
				} else {
					System.out.println("Member: Received a watcher with a path not expected");
				}

				//System.out.println("-----------------------------------------------");
			} catch (Exception e) {
				System.out.println("Exception Member: " + e);
			}
		}
	};

	public void configure() {
		// This is static. A list of zookeeper can be provided for decide where to connect

		// Select a random zookeeper server
		Random rand = new Random();
		int i = rand.nextInt(hosts.length);

		// Create the session
		// Create a session and wait until it is created.
		// When is created, the watcher is notified
		try {
			if (zk == null) {
				zk = new ZooKeeper(hosts[i], SESSION_TIMEOUT, this);
				// We initialize the mutex Integer just after creating ZK.
				try {
					// Wait for creating the session. Use the object lock
					synchronized(mutex) {
						mutex.wait();
					}
					//zk.exists("/", false);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} catch (Exception e) {
			System.out.println("Exception in constructor");
		}

		if (zk == null) {
			return;
		}

		// MEMBERS
		try {
			Stat s = zk.exists(rootMembers, false);
			if (s == null) {
				zk.create(rootMembers, new byte[0],
						Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}

			listMembers = zk.getChildren(rootMembers, memberWatcher, s);


		} catch (KeeperException e) {
			System.out.println("The session with Zookeeper failes. Closing");
			return;
		} catch (InterruptedException e) {
			System.out.println("InterruptedException raised");
		}


		// TRANSACTIONS
		try {
			Stat s = zk.exists(rootTransactions, false);
			if (s == null) {
				zk.create(rootTransactions, new byte[0],
						Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			listMembers = zk.getChildren(rootTransactions, transactionsWatcher, s);

		} catch (KeeperException e) {
			System.out.println("Keeper exception when instantiating queue: "
					+ e.toString());
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception");
		}


		// Create threads
		ProcessMember pm = new ProcessMember(zk, memberWatcher, mutexMember);
		pm.start();
		ProcessBarrier bm = new ProcessBarrier(zk, transactionsWatcher, mutexBarrier);
		bm.start();
	}

	Watcher transactionsWatcher = new Watcher() {
		public void process(WatchedEvent event) {

			System.out.println("------------------Watcher TRANSACTION BARRIER ------------------");
			System.out.println("Barrier: " + event.getType() + ", " + event.getPath());

			try {
				Stat s = zk.exists(rootTransactions, this);

				if (event.getPath().equals(rootTransactions)) {
					listTransactions = zk.getChildren(rootTransactions, this, s);
					System.out.println(listTransactions);
					synchronized (mutexBarrier) {
						nTransactions++;


						System.out.println("New transaction");

						for (String node : listTransactions) {

//							System.out.println(node);

							String[] params = node.split("-");

							if (params[2].equals(myHostName)) {
								System.out.println("Already watching");
								continue;
							}

							String zNode = "/transactions/" + node;

							byte[] data = zk.getData(zNode, this, s);

							BankClientI client = SerializationUtils.deserialize(data);

							if (processBarrier(zNode)) {
								System.out.println("-------------");
								System.out.println(params[0]);
								System.out.println("-------------");


								bank.setNotify(false);

								if (params[0].equals("create")) {
									bank.create(client);
								}

								if (params[0].equals("update")) {
									bank.update(client);
								}

								if (params[0].equals("delete")) {
									bank.delete(client.getAccount());
								}

								try {
									Stat s2 = zk.exists(zNode, false);

									List children = zk.getChildren(zNode, false);

									if (children.isEmpty())
										zk.delete(zNode, s2.getVersion());


								} catch (Exception e) {
									System.out.println(e);
								}

							}

						}

						bank.setNotify(true);
						mutexBarrier.notify();

					}
				} else {
					System.out.println("Barrier: Received a watcher with a path not expected");
				}
			} catch (Exception e) {
				System.out.println("Exception Transaction: " + e);
			}
		}
	};


	public ExploreZk(Bank bank, String myHostName, String[] zkHosts, String[] banks) {
		this.bank = bank;
		this.myHostName = myHostName;
		this.hosts = zkHosts;
		this.banks = banks;
	}

	// Assigned to members
	public void process(WatchedEvent event) {
		Stat s = null;

		System.out.println("------------------Watcher PROCESS ------------------");
		System.out.println("Member: " + event.getType() + ", " + event.getPath());
		try {
			if (event.getPath() == null) {

				listMembers = zk.getChildren(rootMembers, this, s);
				//if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
				System.out.println("SyncConnected");
				synchronized (mutex) {


					for (String node : listMembers) {
						if (node.equals(myHostName)) {
							System.out.println("No sync");
							continue;
						}

						System.out.println("Needs syncing");
						if (node.equals("h1") && !node.equals(myHostName)) {
							setDatabase(banks[0]);
							mutex.notify();
							return;
						}
						if (node.equals("h2") && !node.equals(myHostName)) {
							setDatabase(banks[1]);
							mutex.notify();
							return;
						}
						if (node.equals("h3") && !node.equals(myHostName)) {
							setDatabase(banks[2]);
							mutex.notify();
							return;
						}


					}


					mutex.notify();
				}
			}
			System.out.println("-----------------------------------------------");
		} catch (Exception e) {
			System.out.println("Exception Process: " + e);
		}
	}

	private void setDatabase(String server) {
		try {

			String dir = "http://" + server + "/syncClients";
			System.out.println("Syncing new host");
			URL url = new URL(dir);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			InputStream res = con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(res));

			StringBuffer resp = new StringBuffer();

			String line;

			while ((line = rd.readLine()) != null) {
				resp.append(line);
				resp.append('\r');
			}
			rd.close();

			byte[] decodedOutput = Base64.getMimeDecoder().decode(resp.toString());

			Map<Integer, BankClientI> mapDB = SerializationUtils.deserialize(decodedOutput);

			Database.clients = mapDB;

            con.disconnect();

            String dir2 = "http://" + server + "/syncNames";
            System.out.println("Syncing new host: names");
            URL url2 = new URL(dir2);
            HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
            con2.setRequestMethod("GET");

            InputStream res2 = con2.getInputStream();
            BufferedReader rd2 = new BufferedReader(new InputStreamReader(res2));

            StringBuffer resp2 = new StringBuffer();

            String line2;

            while ((line2 = rd2.readLine()) != null) {
                resp2.append(line2);
                resp2.append('\r');
            }
            rd2.close();

            byte[] decodedOutput2 = Base64.getMimeDecoder().decode(resp2.toString());

            Map<String, Integer> mapDB2 = SerializationUtils.deserialize(decodedOutput2);

            Database.namesMapping = mapDB2;

            con2.disconnect();

			System.out.println(mapDB);
            System.out.println(mapDB2);


        } catch (Exception e) {
            System.out.println("SetDatabase: " + e);
        } finally {

        }

	}

	private boolean processBarrier(String zNode) {

		Stat s = null;
		List members = null;

		try {
			s = zk.exists(rootMembers, false);
			members = zk.getChildren(rootMembers, memberWatcher, s);
		} catch (Exception e) {
			System.out.println(e);
		}

		Barrier b = new Barrier(zNode, members.size(), this.myHostName, this.hosts);

		try{
			boolean flag = b.enter();
			System.out.println("Entered barrier: ");
			if(!flag) System.out.println("Error when entering the barrier");
		} catch (Exception e){
			System.out.println("Enter error: " + e);
		}

		// Generate random integer
		Random rand = new Random();
		int r = rand.nextInt(100);
		// Loop for rand iterations
		for (int i = 0; i < r; i++) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		try{
			b.leave();
		} catch (Exception e){
			System.out.println("Leave error: " + e);
		}
		System.out.println("Left barrier");

		return true;
	}

	private void printListMembers (List<String> list) {
		System.out.println("Remaining # members:" + list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");
		}
		System.out.println();
	}

//	public static void main(String[] args) {
//		ExploreZk eZk = new ExploreZk("host1");
//		eZk.configure();
//	}
}



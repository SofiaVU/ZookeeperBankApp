package es.upm.dit.cnvr.zkBank.zk;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper; 
import org.apache.zookeeper.data.Stat;

// This is a simple application for detecting the correct processes using ZK. 
// Several instances of this code can be created. Each of them detects the 
// valid numbers.

// Two watchers are used:
// - cwatcher: wait until the session is created. 
// - watcherMember: notified when the number of members is updated

// the method process has to be created for implement Watcher. However
// this process should never be invoked, as the "this" watcher is used

public class zkMember implements Watcher{
	private static final int SESSION_TIMEOUT = 5000;

	private static String rootMembers = "/members";
	private static String rootTransactions = "/transactions";
	private String myId;
	
	// This is static. A list of zookeeper can be provided for decide where to connect
	String[] hosts;

	private ZooKeeper zk;

	public zkMember(String myHostName, String[] zkHosts) {

		this.hosts = zkHosts;

		// Select a random zookeeper server
		Random rand = new Random();
		int i = rand.nextInt(hosts.length);

		// Create a session and wait until it is created.
		// When is created, the watcher is notified
		try {
			if (zk == null) {
				zk = new ZooKeeper(hosts[i], SESSION_TIMEOUT, cWatcher);
				try {
					// Wait for creating the session. Use the object lock
					wait();
					//zk.exists("/",false);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} catch (Exception e) {
			System.out.println("Error");
		}

		// Add the process to the members in zookeeper

		if (zk != null) {
			// Create a folder for members and include this process/server
			try {
				// Create a folder, if it is not created
				String response = new String();
				Stat s = zk.exists(rootMembers, watcherMember); //this);
				if (s == null) {
					// Created the znode, if it is not created.
					response = zk.create(rootMembers, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					System.out.println(response);
				}

				// Create a znode for registering as member and get my id
				myId = zk.create(rootMembers + "/" + myHostName, new byte[0],
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

				myId = myId.replace(rootMembers + "/", "");

//				List<String> list = zk.getChildren(rootMembers, watcherMember, s); //this, s);
				System.out.println("Created znode nember id: " + myId);
//				printListMembers(list);
			} catch (KeeperException e) {
				System.out.println("The session with Zookeeper failes. Closing");
				return;
			} catch (InterruptedException e) {
				System.out.println("InterruptedException raised");
			}

		}



		// TRANSACTIONS
		if (zk != null) {
			try {
				Stat s = zk.exists(rootTransactions, transactionsWatcher);
				if (s == null) {
					zk.create(rootTransactions, new byte[0],
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
//				listMembers = zk.getChildren(rootTransactions, transactionsWatcher, s);

			} catch (KeeperException e) {
				System.out.println("Keeper exception when instantiating queue: "
						+ e.toString());
			} catch (InterruptedException e) {
				System.out.println("Interrupted exception");
			}
		}
	}

	// Notified when the session is created
	private Watcher cWatcher = new Watcher() {
		public void process (WatchedEvent e) {
			System.out.println("Created session");
			System.out.println(e.toString());
			notify();
		}
	};

	// Notified when the number of children in /member is updated
	private Watcher  watcherMember = new Watcher() {
		public void process(WatchedEvent event) {
			//System.out.println("------------------Watcher Member------------------\n");
			try {
				System.out.println("New member: " );
				List<String> list = zk.getChildren(rootMembers,  watcherMember); //this);
				printListMembers(list);
			} catch (Exception e) {
				System.out.println("Exception: wacherMember");
			}
		}
	};

	// Notified when the number of children in /transactions is updated
	private Watcher  transactionsWatcher = new Watcher() {
		public void process(WatchedEvent event) {
			System.out.println("------------------Watcher Member------------------\n");
			try {
				System.out.println("New transaction: " );
				List<String> list = zk.getChildren(rootMembers,  watcherMember); //this);
				printListMembers(list);
			} catch (Exception e) {
				System.out.println("Exception: wacherMember");
			}
		}
	};
	
	@Override
	public void process(WatchedEvent event) {
		try {
			System.out.println("Unexpected invocated this method. Process of the object");
			List<String> list = zk.getChildren(rootMembers, watcherMember); //this);
			printListMembers(list);
		} catch (Exception e) {
			System.out.println("Unexpected exception. Process of the object");
		}
	}
	
	private void printListMembers (List<String> list) {
		//System.out.println("Remaining # members:" + list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");				
		}
		System.out.println();
	}
	
//	public static void main(String[] args) {
//		zkMember zk = new zkMember();
//
//		try {
//			Thread.sleep(300000);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
}

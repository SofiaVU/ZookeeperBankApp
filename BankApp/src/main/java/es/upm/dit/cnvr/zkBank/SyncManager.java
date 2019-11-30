package es.upm.dit.cnvr.zkBank;

import es.upm.dit.cnvr.zkBank.model.BankClientI;
import es.upm.dit.cnvr.zkBank.model.Client;
import es.upm.dit.cnvr.zkBank.zk.Barrier;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Random;

public class SyncManager {

    final String[]  hosts;
    private static final int SESSION_TIMEOUT = 5000;
    private ZooKeeper zk;
    private final String myHostName;


    public SyncManager(String[] zkHosts, String myHostName) {

        this.hosts = zkHosts;
        this.myHostName = myHostName;


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

    }
    // Notified when the session is created
    private Watcher cWatcher = new Watcher() {
        public void process (WatchedEvent e) {
            System.out.println("Created session");
            System.out.println(e.toString());
            notify();
        }
    };

    private String createNode(String operationType, BankClientI client) {

        String zNode = "/transactions/" + operationType + "-" + client.getAccount() + "-" + myHostName;

        if (zk == null) {
            return null;
        }

        byte[] data = SerializationUtils.serialize((Client) client);

        String path = null;

        try {
            path = zk.create(zNode, data,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        } catch (Exception e) {
            System.out.println("Exception when instantiating queue: "
                    + e.toString());
        }

        return path;
    }


    // This is static. A list of zookeeper can be provided for decide where to connect
    private boolean processBarrier(String zNode) {


        Stat s = null;
        List members = null;

        try {
            s = zk.exists("/members", false);
            members = zk.getChildren("/members", false, s);
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

    public boolean notifyChanges(BankClientI client, String operationType) {
        String zNode = createNode(operationType, client);

        System.out.println(zNode);

        //Notify the ZK ensemble. Create the barrier. Not updating file until everyone is notified
        if (processBarrier(zNode)) {
                return true;
        }
        return false;
    }

    public boolean cleanup() {

        try {
            Stat s = zk.exists("/transactions", false);
            zk.delete("/transactions", s.getVersion());

            String path = zk.create("/transactions", new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            if (!path.equals(null)) {
                System.out.println("Reset /transactions");
                return true;
            }

            return false;

        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }




}

package es.upm.dit.cnvr.zkBank.zk;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Barrier implements Watcher {

    static ZooKeeper zk = null;

    private int size;
    private String name;
    String root;
    int nWatchers;

    Integer mutexBarrier = -1;


    /**
     * Barrier constructor
     *
     * @param root
     * @param size
     */


    public Barrier(String root, int size, String name, String[] zkHosts) {


        // Select a random zookeeper server
        Random rand = new Random();
        int i = rand.nextInt(zkHosts.length);

        if(zk == null){
            try {
//                System.out.println("Starting ZK:");
                zk = new ZooKeeper(zkHosts[i], 3000, this);

//                System.out.println("Finished starting ZK: " + zk);
            } catch (IOException e) {
                System.out.println(e.toString());
                zk = null;
            }
        }
        //else mutex = new Integer(-1);

        this.root = root;
        this.size = size;

        // Create barrier node
        if (zk != null) {
            try {
                Stat s = zk.exists(root, false);
                if (s == null) {
                    zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                System.out
                        .println("Keeper exception when instantiating queue: "
                                + e.toString());
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception");
            }
        }

        // My node name
//        try {
//            //hostname
////            name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
//        } catch (UnknownHostException e) {
//            System.out.println(e.toString());
//        }

        this.name = name;
    }


    public void process(WatchedEvent event) {
        nWatchers++;
        System.out.println(">>> Process: " + event.toString() + ", " + nWatchers);
        //System.out.println("Process: " + event.getType());
        synchronized (mutexBarrier) {
            mutexBarrier.notify();
        }
    }


    /**
     * Join barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */

    public boolean enter() throws KeeperException, InterruptedException{
        zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);
        zk.exists(root + "/" + name, this);

        while (true) {
            List<String> list = zk.getChildren(root, true);

            if (list.size() < size) {
                synchronized (mutexBarrier) {
                    mutexBarrier.wait();
                }
            } else {
                return true;
            }
        }
    }

    /**
     * Wait until all reach barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */

    public boolean leave() throws KeeperException, InterruptedException{
        zk.delete(root + "/" + name, 0);
        while (true) {
            List<String> list = zk.getChildren(root, true);
            if (list.size() > 0) {
                synchronized (mutexBarrier) {
                    mutexBarrier.wait();
                }
            } else {
                return true;
            }
        }
    }

    public boolean processBarrier(Barrier b) {
        try{
            boolean flag = b.enter();
            System.out.println("Entered barrier: ");
            if(!flag) System.out.println("Error when entering the barrier");
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }

        // Generate random integer
        Random rand = new Random();
        int r = rand.nextInt(100);
        // Loop for rand iterations
        for (int i = 0; i < r; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
        try{
            b.leave();
        } catch (KeeperException e){
            System.out.println("Barrier leave:" +e);
        } catch (InterruptedException e){

        }
        System.out.println("Left barrier");

        return true;
    }

//    public static void main(String[] args) {
//        Barrier b = new Barrier("localhost", "/transactions/create-583-host1", 2, "host2");
//        b.processBarrier(b);
//    }
}
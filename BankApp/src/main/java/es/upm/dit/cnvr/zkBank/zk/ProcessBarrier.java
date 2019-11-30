package es.upm.dit.cnvr.zkBank.zk;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class ProcessBarrier extends Thread {

	private List<String> listBarriersP = null;
	private int npBarriers = 0;
	private String rootTransactions = "/transactions";
	private ZooKeeper zk; 
	private Watcher barrierWatcherP;
	private Integer mutex;

	public ProcessBarrier(ZooKeeper zk, Watcher barrierWatcherP, Integer mutex) {
		this.zk = zk;
		this.barrierWatcherP = barrierWatcherP;
		this.mutex = mutex;			
	}

	@Override
	public void run() {
		Stat s = null;
		while (true) {
			try {
				synchronized (mutex) {
					mutex.wait();
				}
				listBarriersP = zk.getChildren(rootTransactions, barrierWatcherP, s);
				npBarriers ++;
				//System.out.println("Process Barrier. NBarriers: " + npBarriers);
				System.out.println("Current Barriers of processes waiting: " + listBarriersP.size());
			} catch (Exception e) {
				System.out.println("Unexpected Exception process barrier: " + e);
				break;
			}	
		}
	}
}
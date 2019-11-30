package es.upm.dit.cnvr.zkBank;

import es.upm.dit.cnvr.zkBank.model.BankClientI;
import es.upm.dit.cnvr.zkBank.model.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Bank implements ClientDBI<BankClientI> {

    private SyncManager sm;

    private boolean notify;

    public Bank(String[] zkHosts, String myHostName) {
        sm = new SyncManager(zkHosts, myHostName);
        this.notify = true;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    @Override
    public ServiceStatus create(BankClientI client) {

        boolean checkIfClientExists = Database.namesMapping.get(client.getName()) != null;

        // If client name exists, return
        if (checkIfClientExists) {
            return ServiceStatus.CLIENT_EXISTED;
        }

        // Assign new ID
        Client c = (Client) client;

        if(client.getAccount() < 1) {
            c.setAccount(generateId());
        }

        boolean syncOk;

        // Sync with ZK ensemble
        if (!notify) {
            syncOk = true;
        } else {
            syncOk = sm.notifyChanges(c, "create");
        }

        // Once everyone is notified, update DB
        if (syncOk) {

            Database.clients.put(c.getAccount(), c);
            Database.namesMapping.put(c.getName(), c.getAccount());
            return ServiceStatus.OK;
        }

        return ServiceStatus.INFORMATION_INVALID;

    }

    private int generateId() {
        // Generate ID
        Random random = new Random();
        int id = random.nextInt(1000);

        // Check if that ID is already used
        while (read(id) != null) {
            id = random.nextInt(1000);
        }

        return id;
    }

    @Override
    public ServiceStatus update(BankClientI client) {

        if(read(client.getAccount()) == null)
            return ServiceStatus.INFORMATION_INVALID;

        boolean syncOk;

        // Sync with ZK ensemble
        if (!notify) {
            syncOk = true;
        } else {
            syncOk = sm.notifyChanges(client, "update");
        }

        if (syncOk) {
            Database.clients.put(client.getAccount(), client);
            return ServiceStatus.OK;
        }

        return ServiceStatus.INFORMATION_INVALID;

    }

    @Override
    public BankClientI read(String name) {
        int id = Database.namesMapping.get(name);
        return Database.clients.get(id);
    }

    @Override
    public BankClientI read(int account) {
        return Database.clients.get(account);
    }

    @Override
    public ServiceStatus delete(int account) {

        if(read(account) == null)
            return ServiceStatus.INFORMATION_INVALID;


        BankClientI client = Database.clients.get(account);


        boolean syncOk;

        // Sync with ZK ensemble
        if (!notify) {

            syncOk = true;
        } else {
            syncOk = sm.notifyChanges(client, "delete");
        }


        if (syncOk) {
            Database.clients.remove(account);
            Database.namesMapping.remove(client.getName());
            return ServiceStatus.OK;
        }

        return ServiceStatus.INFORMATION_INVALID;
    }

    public List<BankClientI> getClients() {
        List<BankClientI> c = new ArrayList(Database.clients.values());
        return c;
    }

    public void setClients(Map<Integer, BankClientI> clients) {
        Database.clients = clients;
    }

    public boolean bankCleanup() {
        if (sm.cleanup())
            return true;
        return false;
    }

    @Override
    public String toString() {

        String print = "";

        for (BankClientI client : Database.clients.values()) {
            print += client + "\n";
        }

        return print;
    }

}

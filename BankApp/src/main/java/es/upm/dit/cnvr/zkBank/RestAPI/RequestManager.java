package es.upm.dit.cnvr.zkBank.RestAPI;

import es.upm.dit.cnvr.zkBank.Bank;
import es.upm.dit.cnvr.zkBank.Database;
import es.upm.dit.cnvr.zkBank.ServiceStatus;
import es.upm.dit.cnvr.zkBank.model.BankClientI;
import es.upm.dit.cnvr.zkBank.model.Client;

import com.google.gson.*;
import es.upm.dit.cnvr.zkBank.zk.ExploreZk;
import es.upm.dit.cnvr.zkBank.zk.zkMember;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;


public class RequestManager {

    public static void main(String[] args) {

        // args[0] port
        // args[1] myHostName
        // args[2] nServers



        int port = Integer.parseInt(args[0]);
        String myHostName = args[1];
//        String[] zkHosts = {"10.0.0.2:2181", "10.0.0.3:2181", "10.0.0.4:2181"};
//        String[] banks = {"10.0.0.5:80", "10.0.0.6:80", "10.0.0.7:80"};


//        Localhost
//        String[] zkHosts = {"localhost:2181", "localhost:2182", "localhost:2183"};
//        String[] banks = {"localhost:81", "localhost:82", "localhost:83"};


        //Deployment in docker
        String[] zkHosts = {"zoo1:2181", "zoo2:2181", "zoo3:2181"};
        String[] banks = {"bank1:80", "bank2:80", "bank3:80"};

        Bank bank = new Bank(zkHosts, myHostName);
        Gson gson = new Gson();

        port(port);

        // Modify DB in explore when receiving a watcher. Sending BANK as parameter
        zkMember zkMember = new zkMember(myHostName, zkHosts);

        ExploreZk eZk = new ExploreZk(bank, myHostName, zkHosts, banks);
        eZk.configure();


        // API
        post("/create", (req, res) -> {
            String body = req.body();

            try {
                Client client = gson.fromJson(body, Client.class);

                CompletableFuture.runAsync(() -> bank.create(client));

                res.status(200);
                return res.status();

            } catch (Exception e) {
                System.out.println(e);
            }

            res.status(500);
            return res.status();

        });


        get("/readByName", (req, res) -> {

            String name = req.queryParams("name");

            BankClientI c = bank.read(name);

            if (c == null) {
                res.status(404);
                return res.status();
            }

            res.body(c.toString());
            res.status(200);
            return res.body();
        });


        get("/readById", (req, res) -> {

            int id = Integer.parseInt(req.queryParams("id"));

            BankClientI c = bank.read(id);

            if (c == null) {
                res.status(404);
                return res.status();
            }

            res.body(c.toString());
            res.status(200);
            return res.body();
        });

        post("/update", (req, res) -> {

            String body = req.body();

            try {
                Client client = gson.fromJson(body, Client.class);

                CompletableFuture.runAsync(() -> bank.update(client));

                res.type("application/json");
                res.status(200);
                return res.status();

            } catch (Exception e) {
                System.out.println(e);
            }

            res.status(500);
            return res.status();

        });

        post("/delete", (req, res) -> {
            String body = req.body();

            try {
                Client client = gson.fromJson(body, Client.class);

                CompletableFuture.runAsync(() -> {
                    bank.delete(client.getAccount());
                });

                res.type("application/json");
                res.status(200);
                return res.status();

            } catch (Exception e) {
                System.out.println(e);
            }

            res.status(500);
            return res.status();

        });

        get("/getClients", (req, res) -> {
            List<BankClientI> clients = bank.getClients();

            String clientsJSON = gson.toJson(clients);

            return clientsJSON;
        });

        get("/syncClients", (req, res) -> {
            byte[] b = SerializationUtils.serialize((Serializable) Database.clients);
            String encodedInput = Base64.getEncoder().encodeToString(b);

            return encodedInput;
        });

        get("/syncNames", (req, res) -> {
            byte[] b = SerializationUtils.serialize((Serializable) Database.namesMapping);
            String encodedInput = Base64.getEncoder().encodeToString(b);

            return encodedInput;
        });

        get("/cleanup", (req, res) -> {

            bank.bankCleanup();
            return 200;
        });



    }
}
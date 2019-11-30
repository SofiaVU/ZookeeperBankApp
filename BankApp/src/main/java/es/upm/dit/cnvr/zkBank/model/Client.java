package es.upm.dit.cnvr.zkBank.model;


import java.io.Serializable;

public class Client implements BankClientI, Serializable {

    private int id;
    private String name;
    private int balance;

    public Client(String name) {
        this.id = -1;
        this.name = name;
        this.balance = 0;
    }

    @Override
    public int getAccount() {
        return id;
    }

    @Override
    public void setAccount(int account) {
        this.id = account;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getBalance() {
        return this.balance;
    }

    @Override
    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Client: " + this.id + "// Name: " + this.name + " // Balance: " + this.balance;
    }
}

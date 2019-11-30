package es.upm.dit.cnvr.zkBank.model;

public interface BankClientI {
    int getAccount();
    void setAccount(int account);
    String getName();
    void setName(String name);
    int getBalance();
    void setBalance(int balance);
    String toString();
}

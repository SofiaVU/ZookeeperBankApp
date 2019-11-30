package es.upm.dit.cnvr.zkBank;

import es.upm.dit.cnvr.zkBank.ServiceStatus;

public interface ClientDBI<BankClientI> {
    ServiceStatus create(BankClientI client);
    ServiceStatus update(BankClientI client);
    BankClientI read(String name);
    BankClientI read (int account);
    ServiceStatus delete (int account);

}

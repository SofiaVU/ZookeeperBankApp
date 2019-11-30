package es.upm.dit.cnvr.zkBank;

import es.upm.dit.cnvr.zkBank.model.BankClientI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Database implements Serializable {

    public static Map<Integer, BankClientI> clients = new HashMap<>();
    public static Map<String, Integer> namesMapping = new HashMap<>();

}

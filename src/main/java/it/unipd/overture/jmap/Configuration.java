package it.unipd.overture.jmap;

import java.util.LinkedList;

public class Configuration {
  public String getDbName() {
    return getDomain().split("\\.")[0];
  }

  public String getDomain() {
    return System.getenv("DOMAIN");
  }

  public String getDatabase() {
    return System.getenv("DATABASE");
  }

  public LinkedList<String[]> getAccounts() {
    var accounts = new LinkedList<String[]>();
    for (var acc : System.getenv("ACCOUNTS").split(",")) {
      accounts.add(acc.split(":"));
    }
    return accounts;
  }
}

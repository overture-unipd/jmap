package it.unipd.overture.jmap;

import it.unipd.overture.jmap.entities.Account;
import java.util.LinkedList;

public class Configuration {
  public String getDomain() {
    return System.getenv("DOMAIN");
  }

  public String getDatabase() {
    return System.getenv("DATABASE");
  }

  public LinkedList<Account> getAccounts() {
    var accounts = new LinkedList<Account>();
    for (var acc : System.getenv("ACCOUNTS").split(",")) {
      var t = acc.split(":");
      accounts.add(Account.builder().address(t[0]).password(t[1]).build());
    }
    return accounts;
  }
}

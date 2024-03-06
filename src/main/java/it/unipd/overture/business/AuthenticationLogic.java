package it.unipd.overture.business;

import com.google.inject.Inject;
import it.unipd.overture.ports.out.AccountPort;

public class AuthenticationLogic {
  AccountPort account;

  @Inject
  AuthenticationLogic(AccountPort account) {
    this.account = account;
  }

  boolean authenticate(String username, String password) {
    return account.getPassword(account.getId(username)).equals(password);
  }
}

package it.unipd.overture.service;

import com.google.inject.Inject;

import it.unipd.overture.port.out.AccountPort;

public class AuthenticationLogic {
  AccountPort account;

  @Inject
  AuthenticationLogic(AccountPort account) {
    this.account = account;
  }

  public boolean authenticate(String username, String password) {
    return account.getPassword(account.getId(username)).equals(password);
  }
}

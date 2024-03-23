package it.unipd.overture.service;

import com.google.inject.Inject;

import it.unipd.overture.port.out.AccountPort;

public class AuthenticationLogic {
  private AccountPort account;

  @Inject
  AuthenticationLogic(AccountPort account) {
    this.account = account;
  }

  public Boolean authenticate(String username, String password) {
    String accountid = account.getId(username);
    if (accountid == null) {
      return false;
    }
    String pw = account.getPassword(accountid);
    if (pw == null) {
      return false;
    }
    return pw.equals(password);
  }
}

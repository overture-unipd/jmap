package it.unipd.overture.service;

import com.google.inject.Inject;

import it.unipd.overture.port.out.AccountPort;
import it.unipd.overture.port.out.UpdatePort;
import rs.ltt.jmap.mock.server.Update;

public class StateManager {
  AccountPort accountPort;
  UpdatePort updatePort;

  @Inject
  StateManager(AccountPort accountPort, UpdatePort updatePort) {
    this.accountPort = accountPort;
    this.updatePort = updatePort;
  }

  private void incrementState(String accountid) {
    accountPort.incrementState(accountid);
  }

  // String insertUpdate(Update upd) {
    // return accountPort.putUpdate
    // putUpdate(Update )
  // }
}

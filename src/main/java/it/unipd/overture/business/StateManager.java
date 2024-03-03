package it.unipd.overture.business;

import com.google.inject.Inject;

import it.unipd.overture.ports.out.AccountPort;
import it.unipd.overture.ports.out.UpdatePort;
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

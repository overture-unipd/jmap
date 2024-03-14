package it.unipd.overture.service;

import com.google.inject.Inject;

import it.unipd.overture.port.out.IdentityPort;

import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.identity.GetIdentityMethodCall;
import rs.ltt.jmap.common.method.response.identity.GetIdentityMethodResponse;

public class IdentityLogic {
  private IdentityPort identityPort;

  @Inject
  IdentityLogic(IdentityPort identityPort) {
    this.identityPort = identityPort;
  }

  public MethodResponse[] get(GetIdentityMethodCall methodCall) {
    var accountid = methodCall.getAccountId();
    return new MethodResponse[] {
      GetIdentityMethodResponse.builder()
        .list(identityPort.getOf(accountid))
        .build()
    };
  }
}

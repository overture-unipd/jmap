package it.unipd.overture.business;

import com.google.common.collect.ListMultimap;

import it.unipd.overture.ports.out.IdentityPort;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.Response.Invocation;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.identity.GetIdentityMethodCall;
import rs.ltt.jmap.common.method.response.identity.GetIdentityMethodResponse;

public class IdentityLogic {
  IdentityPort identityPort;

  IdentityLogic(IdentityPort identityPort) {
    this.identityPort = identityPort;
  }

  public MethodResponse[] get(GetIdentityMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
    /*
    return new MethodResponse[] {
      GetIdentityMethodResponse.builder()
        .list(
          new IdentityHandler[] {
          IdentityHandler.builder()
            .id(accountid)
            .email(db.getAccountAddress(accountid))
            .name(db.getAccountName(accountid))
            .build()
          })
        .build()
    };
    */
  }
}

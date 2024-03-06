package it.unipd.overture.business;

import com.google.common.collect.ListMultimap;

import it.unipd.overture.ports.out.IdentityPort;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.Response.Invocation;
import rs.ltt.jmap.common.entity.Identity;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.identity.GetIdentityMethodCall;
import rs.ltt.jmap.common.method.response.identity.GetIdentityMethodResponse;

public class IdentityLogic {
  IdentityPort identityPort;

  IdentityLogic(IdentityPort identityPort) {
    this.identityPort = identityPort;
  }

  public MethodResponse[] get(GetIdentityMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    var accountid = methodCall.getAccountId();
    return new MethodResponse[] {
      GetIdentityMethodResponse.builder()
        .list(
          new Identity[] {
          Identity.builder()
            .id(accountid)
            // TODO: get all information for the user identity
            // .email(identityPort.getFirst(null))
            // .name(db.getAccountName(accountid))
            .build()
          })
        .build()
    };
  }
}

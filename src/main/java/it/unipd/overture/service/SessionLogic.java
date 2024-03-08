package it.unipd.overture.service;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;

import it.unipd.overture.port.out.AccountPort;
import rs.ltt.jmap.common.SessionResource;
import rs.ltt.jmap.common.entity.Account;
import rs.ltt.jmap.common.entity.Capability;
import rs.ltt.jmap.common.entity.capability.CoreCapability;
import rs.ltt.jmap.common.entity.capability.MailAccountCapability;
import rs.ltt.jmap.common.entity.capability.MailCapability;

public class SessionLogic {
  Gson gson;
  AccountPort accountPort;

  @Inject
  SessionLogic(Gson gson, AccountPort accountPort) {
    this.gson = gson;
    this.accountPort = accountPort;
  }
  
  public String get(String username) {
    ImmutableMap.Builder<Class<? extends Capability>, Capability> capabilityBuilder =
        ImmutableMap.builder();
    capabilityBuilder.put(
      MailCapability.class,
      MailCapability.builder()
        .build());
    capabilityBuilder.put(
        CoreCapability.class,
        CoreCapability.builder()
          .maxSizeUpload(100 * 1024 * 1024L) // 100MB
          .maxObjectsInGet(1L)
          .maxCallsInRequest(1L)
          .maxObjectsInSet(1L)
          .maxConcurrentUpload(1L)
          .build());
    final String accountid = accountPort.getId(username);
    String server = "https://overture.duckdns.org";
    final SessionResource sessionResource =
        SessionResource.builder()
          .apiUrl(server + "/api/jmap") // TODO: replace with getenv or something else
          .uploadUrl(server + "/api/upload")
          .downloadUrl(server + "/api/download?blobid={blobId}")
          .state(accountPort.getState(username))
          .username(username)
          .eventSourceUrl("")
          .account(
              accountid,
              Account.builder()
                .accountCapabilities(
                  ImmutableMap.of(
                      MailAccountCapability.class,
                      MailAccountCapability.builder()
                          .maxSizeAttachmentsPerEmail(50 * 1024 * 1024L) // 50MiB
                          .build()))
                // .name(address)
                .isPersonal(true)
                .isReadOnly(false)
                .build())
          .capabilities(capabilityBuilder.build())
          .primaryAccounts(ImmutableMap.of(MailAccountCapability.class, accountid))
          .build();
  
    return gson.toJson(sessionResource);
  }
}

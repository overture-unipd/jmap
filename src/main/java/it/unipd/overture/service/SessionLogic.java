package it.unipd.overture.service;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.unipd.overture.port.out.AccountPort;
import it.unipd.overture.port.out.StatePort;
import rs.ltt.jmap.common.SessionResource;
import rs.ltt.jmap.common.entity.Account;
import rs.ltt.jmap.common.entity.Capability;
import rs.ltt.jmap.common.entity.capability.CoreCapability;
import rs.ltt.jmap.common.entity.capability.MailAccountCapability;

public class SessionLogic {
  private AccountPort accountPort;
  private StatePort statePort;

  @Inject
  SessionLogic(AccountPort accountPort, StatePort statePort) {
    this.accountPort = accountPort;
    this.statePort = statePort;
  }
  
  public SessionResource get(String username) {
    ImmutableMap.Builder<Class<? extends Capability>, Capability> capabilityBuilder =
        ImmutableMap.builder();
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
    final SessionResource sessionResource =
        SessionResource.builder()
          .apiUrl("/api/jmap")
          .uploadUrl("/api/upload")
          .downloadUrl("/api/download?blobid={blobId}")
          .state(statePort.get(accountid))
          .username(username)
          .account(
              accountid,
              Account.builder()
                .accountCapabilities(
                  ImmutableMap.of(
                      MailAccountCapability.class,
                      MailAccountCapability.builder()
                          .maxSizeAttachmentsPerEmail(50 * 1024 * 1024L) // 50MiB
                          .build()))
                .name(username)
                .build())
          .capabilities(capabilityBuilder.build())
          .primaryAccounts(ImmutableMap.of(MailAccountCapability.class, accountid))
          .build();
    return sessionResource;
  }
}

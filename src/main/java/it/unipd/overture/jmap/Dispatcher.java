package it.unipd.overture.jmap;

import java.util.Base64;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import rs.ltt.jmap.common.SessionResource;
import rs.ltt.jmap.common.entity.Account;
import rs.ltt.jmap.common.entity.Capability;
import rs.ltt.jmap.common.entity.Upload;
import rs.ltt.jmap.common.entity.capability.CoreCapability;
import rs.ltt.jmap.common.entity.capability.MailAccountCapability;
import rs.ltt.jmap.gson.JmapAdapters;

public class Dispatcher {
  GsonBuilder gsonBuilder;
  Gson gson;

  Dispatcher() {
    gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    gson = gsonBuilder.create();
  }

  public String[] extractAuth(String auth) {
    var encoded = auth.split(" ")[1];
    var decoded = new String(Base64.getDecoder().decode(encoded));
    return decoded.split(":");
  }

  public boolean authenticate(String address, String password) {
    return new Database().getAccountPassword(getAccountId(address)).equals(password);
  }

  private String getAccountId(String address) {
    return new Database().getAccountId(address);
  }

  private String getAccountState(String accountid) {
    return new Database().getAccountState(accountid);
  }

  public String upload(String address, String type, long size, byte[] blob) {
    var blobid = new Database().insertFile(blob);
    final String accountid = getAccountId(address);
    final Upload upload =
      Upload.builder()
        .size(size)
        .accountId(accountid)
        .blobId(blobid)
        .type(type)
        .build();
    return gson.toJson(upload);
  }

  public byte[] download(String blobid) {
    return new Database().getFile(blobid);
  }

  public String session(String address) {
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
    final String accountid = getAccountId(address);
    final SessionResource sessionResource =
        SessionResource.builder()
            .apiUrl("/api/jmap")
            .uploadUrl("/api/upload")
            .downloadUrl("/api/download" + "?blobid={blobId}")
            .state(getAccountState(accountid))
            .username(address)
            .account(
                accountid,
                Account.builder()
                  .accountCapabilities(
                    ImmutableMap.of(
                        MailAccountCapability.class,
                        MailAccountCapability.builder()
                            .maxSizeAttachmentsPerEmail(50 * 1024 * 1024L) // 50MiB
                            .build()))
                  .name(address)
                  .build())
            .capabilities(capabilityBuilder.build())
            .primaryAccounts(ImmutableMap.of(MailAccountCapability.class, accountid))
            .build();

    return gson.toJson(sessionResource);
  }

  public String jmap(String username, String body) {
    return new Jmap(username, body).dispatch();
  }

  public String reset() {
    new Database().reset();
    return "Reset Done";
  }
}
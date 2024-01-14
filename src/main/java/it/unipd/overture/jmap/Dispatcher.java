package it.unipd.overture.jmap;

import java.util.Base64;
import java.util.LinkedList;

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
  private GsonBuilder gsonBuilder;
  private Gson gson;
  private Database db;
  Jmap jmap;

  Dispatcher(Database db) {
    gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    gson = gsonBuilder.create();
    this.db = db;
    jmap = null;
  }

  Dispatcher() {
    this(new Database());
  }

  public String[] extractAuth(String auth) {
    var encoded = auth.split(" ")[1];
    var decoded = new String(Base64.getDecoder().decode(encoded));
    return decoded.split(":");
  }

  public boolean authenticate(String address, String password) {
    return db.getAccountPassword(getAccountId(address)).equals(password);
  }

  private String getAccountId(String address) {
    return db.getAccountId(address);
  }

  private String getAccountState(String accountid) {
    return db.getAccountState(accountid);
  }

  public String upload(String type, long size, byte[] blob) {
    var blobid = db.insertAttachment(blob);
    final Upload upload =
      Upload.builder()
        .size(size)
        .accountId("null")
        .blobId(blobid)
        .type(type)
        .build();
    return gson.toJson(upload);
  }

  public byte[] download(String blobid) {
    return db.getAttachment(blobid);
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

  public String jmap(String address, String body) {
    return jmap.dispatch(body);
  }

  public String reset() {
    var accounts = new LinkedList<String[]>();
    for (var acc : System.getenv("ACCOUNTS").split(",")) {
      accounts.add(acc.split(":"));
    }
    var domain = System.getenv("DOMAIN");
    db.reset(accounts, domain);
    jmap = new Jmap(db, gson, db.getAccountId(accounts.get(0)[0]+"@"+domain));
    jmap.reset(); // reset per primo account
    return "Reset Done";
  }
}

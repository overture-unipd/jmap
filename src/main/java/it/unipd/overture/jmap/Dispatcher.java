package it.unipd.overture.jmap;

import static spark.Spark.halt;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import rs.ltt.jmap.common.SessionResource;
import rs.ltt.jmap.common.entity.Account;
import rs.ltt.jmap.common.entity.Capability;
import rs.ltt.jmap.common.entity.Upload;
import rs.ltt.jmap.common.entity.capability.CoreCapability;
import rs.ltt.jmap.common.entity.capability.MailAccountCapability;
import rs.ltt.jmap.gson.JmapAdapters;
import spark.Request;
import spark.Response;

public class Dispatcher {
  GsonBuilder gsonBuilder;
  Gson GSON;
  Map<String, byte[]> attachments; // TODO: replace with rethinkdb blobs or native files

  Dispatcher() {
    gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    GSON = gsonBuilder.create();
    attachments = new HashMap<>();
  }

  private String[] extractAuth(Request q) { // TODO: use a class istead of array
    if (q.headers("Authorization") == null) {
      return null;
    }
    var encoded = q.headers("Authorization").split(" ")[1];
    var decoded = new String(Base64.getDecoder().decode(encoded));
    return decoded.split(":");
  }

  private String getAccountId(String address) {
    return new Database().getAccountId(address);
  }

  private String getAccountState(String id) {
    return new Database().getAccountState(id);
  }

  public String upload(Request q, Response a) {
    a.type("application/json");
    String contentType = q.headers("Content-Type");
    long size = q.contentLength();
    byte[] blob = q.bodyAsBytes();
    String blobId = Hashing.sha256().hashBytes(blob).toString();
    final String username = extractAuth(q)[0];
    final String accountid = getAccountId(username);
    attachments.put(blobId, blob);
    final Upload upload =
      Upload.builder()
        .size(size)
        .accountId(accountid)
        .blobId(blobId)
        .type(contentType)
        .build();
    return GSON.toJson(upload);
  }

  public String download(Request q, Response a) {
    var blobid = q.queryParams("blobid");
    a.body(new String(attachments.get(blobid)));
    return "";
  }

  public void authenticate(Request q, Response a) {
    String[] t = extractAuth(q);
    if (t == null) {
      halt(401, "Requests need to be authenticated.");
    }
    var username = t[0];
    var password = t[1];
    if (! new Database().getAccountPassword(getAccountId(username)).equals(password)) {
      halt(401, "Wrong credentials.");
    };
  }

  public String session(Request q, Response a) {
    a.type("application/json");
    ImmutableMap.Builder<Class<? extends Capability>, Capability> capabilityBuilder =
        ImmutableMap.builder();
    capabilityBuilder.put(
        CoreCapability.class,
        CoreCapability.builder()
            .maxSizeUpload(1024 * 1024L) // 1MiB
            .maxObjectsInGet(1L) // TODO: raise
            .maxCallsInRequest(1L)
            .maxObjectsInSet(1L)
            .maxConcurrentUpload(1L)
            .build());
    final String username = extractAuth(q)[0];
    final String accountid = getAccountId(username);
    final SessionResource sessionResource =
        SessionResource.builder()
            .apiUrl("/api/jmap")
            .uploadUrl("/api/upload")
            .downloadUrl("/api/download" + "?blobId={blobId}")
            .state(getAccountState(accountid))
            .username(username)
            .account(
                accountid,
                Account.builder()
                  .accountCapabilities(
                    ImmutableMap.of(
                        MailAccountCapability.class,
                        MailAccountCapability.builder()
                            .maxSizeAttachmentsPerEmail(
                                50 * 1024 * 1024L) // 50MiB
                            .build()))
                  .name(username)
                  .build())
            .capabilities(capabilityBuilder.build())
            .primaryAccounts(ImmutableMap.of(MailAccountCapability.class, accountid))
            .build();

    return GSON.toJson(sessionResource);
  }

  public String jmap(Request q, Response a) {
    a.type("application/json");
    var t = new Gson().fromJson(q.body(), Properties[].class);
    for (Properties o : t) {
      o.getProperty("id");
    }
    return "";
  }

  public String reset(Request q, Response a) {
    new Database().reset();
    return "Reset Done";
  }
}

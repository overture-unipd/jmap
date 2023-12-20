package it.unipd.overture.jmap;

import static spark.Spark.halt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;

import okio.Buffer;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.net.MediaType;
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
import spark.utils.IOUtils;

public class Dispatcher {
  GsonBuilder gsonBuilder;
  Gson gson;

  Dispatcher() {
    gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    gson = gsonBuilder.create();
  }

  private String[] extractAuth(Request q) {
    if (q.headers("Authorization") == null) {
      return null;
    }
    var encoded = q.headers("Authorization").split(" ")[1];
    var decoded = new String(Base64.getDecoder().decode(encoded));
    return decoded.split(":");
  }

  public void authenticate(Request q, Response a) {
    String[] t = extractAuth(q);
    if (t == null) {
      halt(401, "Requests have to be authenticated.");
    }
    var username = t[0];
    var password = t[1];
    if (! new Database().getAccountPassword(getAccountId(username)).equals(password)) {
      halt(401, "Wrong credentials.");
    };
  }

  private String getAccountId(String address) {
    return new Database().getAccountId(address);
  }

  private String getAccountState(String accountid) {
    return new Database().getAccountState(accountid);
  }

  public String upload(Request q, Response a) {
    a.type("application/json");
    String contentType = q.headers("Content-Type");
    long size = q.contentLength();
    byte[] blob = q.bodyAsBytes();
    String blobId = Hashing.sha256().hashBytes(blob).toString();
    var db = new Database();
    db.insertFile(blobId, blob);
    final String username = extractAuth(q)[0];
    final String accountid = getAccountId(username);
    final Upload upload =
      Upload.builder()
        .size(size)
        .accountId(accountid)
        .blobId(blobId)
        .type(contentType)
        .build();
    return gson.toJson(upload);
  }

  public Object download(Request q, Response a) {
    var blobId = q.queryParams("blobid");
    var file = new Database().getFile(blobId);

    a.header("Content-Disposition", String.format("attachment; filename=\"%s.raw\"", blobId)); // TODO use filename associated, instead of blobid
    a.type(MediaType.OCTET_STREAM.toString()); // "application/octet-stream"
    a.raw().setContentLength(file.length);

    try {
      var is = new ByteArrayInputStream(file);
      var os = a.raw().getOutputStream();
      IOUtils.copy(is, os);
      is.close();
      os.close();
    } catch (IOException e) {
      halt(500, "Something is wrong with the download.");
    }

    return null;
  }

  public String session(Request q, Response a) {
    a.type("application/json");
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
                            .maxSizeAttachmentsPerEmail(50 * 1024 * 1024L) // 50MiB
                            .build()))
                  .name(username)
                  .build())
            .capabilities(capabilityBuilder.build())
            .primaryAccounts(ImmutableMap.of(MailAccountCapability.class, accountid))
            .build();

    return gson.toJson(sessionResource);
  }

  public String jmap(Request q, Response a) {
    a.type("application/json");
    return new Jmap(getAccountId(extractAuth(q)[0]), q.body()).dispatch();
  }

  public String reset(Request q, Response a) {
    new Database().reset();
    return "Reset Done";
  }
}

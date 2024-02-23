package it.unipd.overture.business;

import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.method.MethodCall;

public class Dispatcher {
  Email email;
  Mailbox mailbox;
  Echo echo;
  Identity identity;
  Account account;
  Database db;

  public String dispatch(String request) {
    // System.out.println(">>>>>\n"+ request + "\n\n");
    var jmapRequest = gson.fromJson(request, Request.class);
    final GenericResponse response = pick(jmapRequest);
    // System.out.println("<<<<<\n"+ gson.toJson(response) + "\n\n");
    return gson.toJson(response);
  }

  private GenericResponse dispatch(final Request request) {
    final var methodCalls = request.getMethodCalls();
    final var using = request.getUsing();
    if (using == null || methodCalls == null) {
      return new ErrorResponse(ErrorType.NOT_REQUEST, 400);
    }
    final ArrayListMultimap<String, Response.Invocation> response = ArrayListMultimap.create();
    for (final Request.Invocation invocation : methodCalls) {
      final String id = invocation.getId();
      final MethodCall methodCall = invocation.getMethodCall();
      for (MethodResponse methodResponse :
          dispatch(methodCall, ImmutableListMultimap.copyOf(response))) {
        response.put(id, new Response.Invocation(methodResponse, id));
      }
    }
    return new Response(
        response.values().toArray(new Response.Invocation[0]), getState());
  }

  void pick (
    MethodCall methodCall,
    ListMultiMap<String, Response.Invocation> prevResponses
  ) {
    return switch(methodCall) {
      case EchoMethodCall call -> {
        yield echo.echo(call, prevResponses);
      }
      case ChangesEmailMethodCall call -> {
        yield email.changes(call, prevResponses);
      }
      case GetEmailMethodCall call -> {
        yield email.get(call, prevResponses);
      }
      case QueryEmailMethodCall call -> {
        yield email.query(call, prevResponses);
      }
      case SetEmailMethodCall call -> {
        yield email.set(call, prevResponses);
      }
      case GetIdentityMethodCall call -> {
        yield identity.get(call, prevResponses);
      }
      case ChangesMailboxMethodCall call -> {
        yield mailbox.changes(call, prevResponses);
      }
      case GetMailboxMethodCall -> {
        yield mailbox.get(call, prevResponses);
      }
      case SetMailboxMethodCall -> {
        yield mailbox.set(call, prevResponses);
      }
      case ChangesThreadMethodCall -> {
        yield threads.changes(call, prevResponses);
      }
      case GetThreadMethodCall -> {
        yield threads.get(call, prevResponses);
      }
      default -> {
        yield new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
      }
    }
  }
}

/*
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
import rs.ltt.jmap.common.entity.capability.MailCapability;
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
    final String accountid = getAccountId(address);
    final SessionResource sessionResource =
        SessionResource.builder()
            .apiUrl("http://localhost:8000/api/jmap")
            .uploadUrl("http://localhost:8000/api/upload")
            .downloadUrl("http://localhost:8000/api/download" + "?blobid={blobId}")
            .state(getAccountState(accountid))
            .username(address)
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
                  .name(address)
                  .isPersonal(true)
                  .isReadOnly(false)
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
    jmap.setupInbox();
    return "Reset Done";
  }
}
 */

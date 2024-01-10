package it.unipd.overture.jmap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import rs.ltt.jmap.common.*;
import rs.ltt.jmap.common.entity.*;
import rs.ltt.jmap.common.method.*;
import rs.ltt.jmap.common.method.call.core.*;
import rs.ltt.jmap.common.method.call.email.*;
import rs.ltt.jmap.common.method.call.identity.*;
import rs.ltt.jmap.common.method.call.mailbox.*;
import rs.ltt.jmap.common.method.call.snippet.*;
import rs.ltt.jmap.common.method.call.submission.*;
import rs.ltt.jmap.common.method.call.thread.*;
import rs.ltt.jmap.common.method.call.vacation.*;
import rs.ltt.jmap.common.method.error.*;
import rs.ltt.jmap.common.method.response.core.*;
import rs.ltt.jmap.common.method.response.email.*;
import rs.ltt.jmap.common.method.response.identity.*;
import rs.ltt.jmap.mock.server.CreationIdResolver;
import rs.ltt.jmap.mock.server.ResultReferenceResolver;

public class Jmap {
  GsonBuilder gsonBuilder;
  Gson gson;
  String accountid;
  String request;
  Database db;

  Jmap(Database db, Gson gson, String accountid, String request) {
    this.db = db;
    this.gson = gson;
    this.accountid = accountid;
    this.request = request;
  }

  public String dispatch() {
    var jmapRequest = gson.fromJson(request, Request.class);
    final GenericResponse response = dispatch(jmapRequest);
    if (response instanceof ErrorResponse) {
      return gson.toJson(response); // should give an error 400 along with the response
    }
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

   private Map<String, Email> getEmails() {
    var json = db.getTable("email");
    var map = new HashMap<String, Email>();
    for (var el : json) {
      var m = gson.fromJson(el, Email.class);
      map.put(m.getId(), m);
    }
    return map;
  }
  private String insertEmail(String id, Email email) { // TODO: id is not really used
    return db.insertInTable("email", gson.toJson(email));
  }


   private void createEmail(final Email email) {
    // db.createEmail(accountid, gson.toJson(email));
    // db.createEmail(accountid, email.getId(), gson.toJson(email));
    // TODO: either new emailid or return the one set by the database
    incrementState();
  }

  private String getState() {
    return new Database().getAccountState(accountid);
  }

  private Stream<Email> getAccountEmails(String test) {
    // var emails = db.getAccountEmails(accountid);
    var emails = "";
    Type listType = new TypeToken<List<Email>>() {}.getType();
    List<Email> list = gson.fromJson(emails, listType);
    return list.stream();
  }

  private Email getEmail(String id) {
    //  var email = db.getEmail(id);
    var email = "";
    return  gson.fromJson(email, Email.class);
  }

  private void incrementState() {
    db.incrementAccountState(accountid);
  }

  private MethodResponse[] dispatch(
      final MethodCall methodCall,
      final ListMultimap<String, Response.Invocation> previousResponses) {
    /** jmap-core */
    if (methodCall instanceof EchoMethodCall) {
      return execute((EchoMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Email */
    if (methodCall instanceof ChangesEmailMethodCall) {
      return execute((ChangesEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetEmailMethodCall) {
      return execute((GetEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof QueryChangesEmailMethodCall) {
      return execute((QueryChangesEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof QueryEmailMethodCall) {
      return execute((QueryEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof SetEmailMethodCall) {
      return execute((SetEmailMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Identity */
    if (methodCall instanceof GetIdentityMethodCall) {
      return execute((GetIdentityMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Mailbox */
    if (methodCall instanceof ChangesMailboxMethodCall) {
      return execute((ChangesMailboxMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetMailboxMethodCall) {
      return execute((GetMailboxMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof SetMailboxMethodCall) {
      return execute((SetMailboxMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Thread */
    if (methodCall instanceof ChangesThreadMethodCall) {
      return execute((ChangesThreadMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetThreadMethodCall) {
      return execute((GetThreadMethodCall) methodCall, previousResponses);
    }

    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  private MethodResponse[] execute(
      EchoMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {
      EchoMethodResponse.builder().libraryName(methodCall.getLibraryName()).build()
    };
  }

  private MethodResponse[] execute(
      ChangesEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
    // TODO: serve (disattiva funzionalitá starred, drafts, deleted etc)
  }

  private MethodResponse[] execute(
      GetEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    final Request.Invocation.ResultReference idsReference = methodCall.getIdsReference();
    final List<String> ids;
    if (idsReference != null) {
      try {
        ids =
            Arrays.asList(
                ResultReferenceResolver.resolve(idsReference, previousResponses));
      } catch (final IllegalArgumentException e) {
        return new MethodResponse[] {new InvalidResultReferenceMethodErrorResponse()};
      }
    } else {
      ids = Arrays.asList(methodCall.getIds());
    }
    final String[] properties = methodCall.getProperties();

    Stream<Email> emailStream = ids.stream().map(this::getEmail);
    // Stream<Email> emailStream = ids.stream().map(this::getAccountEmails); // TODO: prefer an implementation along these lines: more efficient
    // Stream<Email> emailStream = ids.stream().map(emails::get);

    if (Arrays.equals(properties, Email.Properties.THREAD_ID)) {
      emailStream =
          emailStream.map(
              email ->
                  Email.builder()
                      .id(email.getId())
                      .threadId(email.getThreadId())
                      .build());
    } else if (Arrays.equals(properties, Email.Properties.MUTABLE)) {
      emailStream =
          emailStream.map(
              email ->
                  Email.builder()
                      .id(email.getId())
                      .keywords(email.getKeywords())
                      .mailboxIds(email.getMailboxIds())
                      .build());
    }
    return new MethodResponse[] {
      GetEmailMethodResponse.builder()
          .list(emailStream.toArray(Email[]::new))
          .state(db.getAccountState(accountid))
          .build()
    };
  }

  private MethodResponse[] execute(
      QueryChangesEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  private MethodResponse[] execute(
      QueryEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
    // TODO: serve (spariscono le mail)
  }

  private MethodResponse[] execute(
      SetEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    final String ifInState = methodCall.getIfInState();
    final Map<String, Map<String, Object>> update = methodCall.getUpdate();
    final Map<String, Email> create = methodCall.getCreate();
    final String[] destroy = methodCall.getDestroy();
    if (destroy != null && destroy.length > 0) {
      throw new IllegalStateException("MockMailServer does not know how to destroy");
    }
    final SetEmailMethodResponse.SetEmailMethodResponseBuilder responseBuilder =
        SetEmailMethodResponse.builder();
    final String oldState = getState();
    if (ifInState != null) {
      if (!ifInState.equals(oldState)) {
        return new MethodResponse[] {new StateMismatchMethodErrorResponse()};
      }
    }
    if (update != null) {
      // TODO: implement it
    }
    if (create != null && create.size() > 0) {
      processCreateEmail(create, responseBuilder, previousResponses);
    }
    return new MethodResponse[] {responseBuilder.build()};
  }

  private void processCreateEmail(
      Map<String, Email> create,
      SetEmailMethodResponse.SetEmailMethodResponseBuilder responseBuilder,
      ListMultimap<String, Response.Invocation> previousResponses) {
    for (final Map.Entry<String, Email> entry : create.entrySet()) {
      final String createId = entry.getKey();
      final String id = UUID.randomUUID().toString();
      final String threadId = UUID.randomUUID().toString();
      final Email userSuppliedEmail = entry.getValue();
      final Map<String, Boolean> mailboxMap = userSuppliedEmail.getMailboxIds();
      final Email.EmailBuilder emailBuilder =
          userSuppliedEmail.toBuilder()
              .id(id)
              .threadId(threadId)
              .receivedAt(Instant.now());
      emailBuilder.clearMailboxIds();
      for (Map.Entry<String, Boolean> mailboxEntry : mailboxMap.entrySet()) {
        final String mailboxId =
            CreationIdResolver.resolveIfNecessary(
                mailboxEntry.getKey(), previousResponses);
        emailBuilder.mailboxId(mailboxId, mailboxEntry.getValue());
      }
      final List<EmailBodyPart> attachments = userSuppliedEmail.getAttachments();
      emailBuilder.clearAttachments();
      if (attachments != null) {
        for (final EmailBodyPart attachment : attachments) {
          final String partId = attachment.getPartId();
          final EmailBodyValue value =
              partId == null ? null : userSuppliedEmail.getBodyValues().get(partId);
          if (value != null) {
            final EmailBodyPart emailBodyPart = injectId(attachment);
           //  db.insertFile(
           //      emailBodyPart.getBlobId(),
           //      value.getValue().getBytes(StandardCharsets.UTF_8));
            emailBuilder.attachment(emailBodyPart);
          } else {
            emailBuilder.attachment(attachment);
          }
        }
      }
      final Email email = emailBuilder.build();
  
      createEmail(email);
      responseBuilder.created(createId, email);
    }
  }

  private static EmailBodyPart injectId(final Attachment attachment) {
    return EmailBodyPart.builder()
      // TODO: either get it from the database after insertion,
      // or insert in the database with a custom id
        .blobId(UUID.randomUUID().toString())
        .charset(attachment.getCharset())
        .type(attachment.getType())
        .name(attachment.getName())
        .size(attachment.getSize())
        .build();
  }

  private MethodResponse[] execute(
      GetIdentityMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {
      GetIdentityMethodResponse.builder()
          .list(
              new Identity[] {
                Identity.builder()
                    .id(accountid)
                    .email(db.getAccountAddress(accountid))
                    .name(db.getAccountName(accountid))
                    .build()
              })
          .build()
    };
  }

  private MethodResponse[] execute(
      ChangesMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
    // serve (spariscono le mail)
  }

  private MethodResponse[] execute(
      GetMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
    // TODO: serve
  }

  private MethodResponse[] execute(
      SetMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
    // TODO: serve (se cancello una mail, riappare)
  }

  private MethodResponse[] execute(
      ChangesThreadMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
    // serve (sembra rompere starred, archive, drafts etc)
  }

  private MethodResponse[] execute(
      GetThreadMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
    // TODO: serve
  }

}

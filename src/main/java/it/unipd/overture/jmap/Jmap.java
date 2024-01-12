package it.unipd.overture.jmap;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import rs.ltt.jmap.common.*;
import rs.ltt.jmap.common.entity.*;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;
import rs.ltt.jmap.common.entity.filter.Filter;
import rs.ltt.jmap.common.method.*;
import rs.ltt.jmap.common.method.call.core.*;
import rs.ltt.jmap.common.method.call.email.*;
import rs.ltt.jmap.common.method.call.identity.*;
import rs.ltt.jmap.common.method.call.mailbox.*;
import rs.ltt.jmap.common.method.call.thread.*;
import rs.ltt.jmap.common.method.error.*;
import rs.ltt.jmap.common.method.response.core.*;
import rs.ltt.jmap.common.method.response.email.*;
import rs.ltt.jmap.common.method.response.identity.*;
import rs.ltt.jmap.common.method.response.mailbox.GetMailboxMethodResponse;
import rs.ltt.jmap.mock.server.CreationIdResolver;
import rs.ltt.jmap.mock.server.EmailGenerator;
import rs.ltt.jmap.mock.server.ResultReferenceResolver;
import rs.ltt.jmap.mock.server.Update;
import rs.ltt.jmap.mua.util.MailboxUtil;

public class Jmap {
  private Database db;
  private Gson gson;
  private String accountid;
  private EmailAddress account;

  Jmap(Database db, Gson gson, String accountid) {
    this.db = db;
    this.gson = gson;
    this.accountid = accountid;
    account = EmailAddress.builder()
                .email(db.getAccountAddress(accountid))
                .name(db.getAccountName(accountid))
                .build();
  }

  public String dispatch(String request) {
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
  private String updateEmail(String id, Email email) {
    return db.replaceInTable("email", id, gson.toJson(email));
  }

  private Map<String, MailboxInfo> getMailboxes() {
    var json = db.getTable("mailbox");
    var map = new HashMap<String, MailboxInfo>();
    for (var el : json) {
      var m = gson.fromJson(el, MailboxInfo.class);
      map.put(m.getId(), m);
    }
    return map;
  }
  private String insertMailbox(String id, MailboxInfo mailbox) {
    return db.insertInTable("mailbox", gson.toJson(mailbox));
  }

  private Map<String, Update> getUpdates() {
    var json = db.getTable("update");
    var map = new LinkedHashMap<String, Update>();
    for (var el : json) {
      var m = gson.fromJson(el, Update.class);
      map.put(String.valueOf(Integer.parseInt(m.getNewVersion())-1), m);
    }
    return map;
  }
  private String insertUpdate(String id, Update mailbox) {
    return db.insertInTable("update", gson.toJson(mailbox));
  }

  private Update getAccumulatedUpdateSince(final String oldVersion) {
    final ArrayList<Update> updates = new ArrayList<>();
    for (Map.Entry<String, Update> updateEntry : getUpdates().entrySet()) {
      if (updateEntry.getKey().equals(oldVersion) || updates.size() > 0) {
        updates.add(updateEntry.getValue());
      }
    }
    if (updates.isEmpty()) {
      return null;
    }
    return Update.merge(updates);
  }

  public void reset() {
    setup(4, 4);
  }

  private void setup(final int numThreads, final int emailCount) {
    MailboxInfo m = new MailboxInfo(UUID.randomUUID().toString(), "Inbox", Role.INBOX);
    insertMailbox(m.getId(), m);
    final String mailboxId = MailboxUtil.find(getMailboxes().values(), Role.INBOX).getId();
    int count = emailCount;
    for (int thread = 0; thread < numThreads; ++thread) {
      final int numInThread = (thread % 4) + 1;
      for (int i = 0; i < numInThread; ++i) {
        final Email email =
          EmailGenerator.get(account, mailboxId, count, thread, i, numInThread);
        insertEmail(email.getId(), email);
        count++;
      }
    }
  }

  private void createEmail(final Email email) {
    insertEmail(email.getId(), email);
    incrementState();
  }

  private String getState() {
    return db.getAccountState(accountid);
  }

  private void incrementState() {
    db.incrementAccountState(accountid);
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
    Stream<Email> emailStream = ids.stream().map(getEmails()::get);
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
        .state(getState())
        .build()
    };
  }

  private MethodResponse[] execute(
      QueryEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    final Filter<Email> filter = methodCall.getFilter();
    Stream<Email> stream = getEmails().values().stream();
    stream = applyFilter(filter, stream);
    stream = stream.sorted(Comparator.comparing(Email::getReceivedAt).reversed());
    if (Boolean.TRUE.equals(methodCall.getCollapseThreads())) {
      stream = stream.filter(distinctByKey(Email::getThreadId));
    }
    final List<String> ids = stream.map(Email::getId).collect(Collectors.toList());
    final String anchor = methodCall.getAnchor();
    final int position;
    if (anchor != null) {
      final Long anchorOffset = methodCall.getAnchorOffset();
      final int anchorPosition = ids.indexOf(anchor);
      if (anchorPosition == -1) {
        return new MethodResponse[] {new AnchorNotFoundMethodErrorResponse()};
      }
      position = Math.toIntExact(anchorPosition + (anchorOffset == null ? 0 : anchorOffset));
    } else {
      position =
        Math.toIntExact(
          methodCall.getPosition() == null ? 0 : methodCall.getPosition());
    }
    final int limit =
      Math.toIntExact(methodCall.getLimit() == null ? 40 : methodCall.getLimit());
    final int endPosition = Math.min(position + limit, ids.size());
    final String[] page = ids.subList(position, endPosition).toArray(new String[0]);
    final Long total =
      Boolean.TRUE.equals(methodCall.getCalculateTotal()) ? (long) ids.size() : null;
    return new MethodResponse[] {
      QueryEmailMethodResponse.builder()
        .canCalculateChanges(false)
        .queryState(getState())
        .total(total)
        .ids(page)
        .position((long) position)
        .build()
    };
  }

  private static Stream<Email> applyFilter(
      final Filter<Email> filter, Stream<Email> emailStream) {
    if (filter instanceof EmailFilterCondition) {
      final EmailFilterCondition emailFilterCondition = (EmailFilterCondition) filter;
      final String inMailbox = emailFilterCondition.getInMailbox();
      if (inMailbox != null) {
        emailStream =
          emailStream.filter(email -> email.getMailboxIds().containsKey(inMailbox));
      }
      final String[] header = emailFilterCondition.getHeader();
      if (header != null
          && header.length == 2
          && header[0].equals("Autocrypt-Setup-Message")) {
        emailStream =
          emailStream.filter(
            email -> header[1].equals(email.getAutocryptSetupMessage()));
      }
    }
    return emailStream;
  }

  private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    final Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
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
      final String[] idsParameter = methodCall.getIds();
      ids = idsParameter == null ? null : Arrays.asList(idsParameter);
    }
    Stream<Mailbox> mailboxStream = getMailboxes().values().stream().map(this::toMailbox);
    return new MethodResponse[] {
      GetMailboxMethodResponse.builder()
        .list(
          mailboxStream
            .filter(m -> ids == null || ids.contains(m.getId()))
            .toArray(Mailbox[]::new))
        .state(getState())
        .build()
    };
  }

  private Mailbox toMailbox(MailboxInfo mailboxInfo) {
    var emails = getEmails();
    return Mailbox.builder()
      .id(mailboxInfo.getId())
      .name(mailboxInfo.getName())
      .role(mailboxInfo.getRole())
      .totalEmails(
        emails.values().stream()
          .filter(e -> e.getMailboxIds().containsKey(mailboxInfo.getId()))
          .count())
      .unreadEmails(
        emails.values().stream()
          .filter(e -> e.getMailboxIds().containsKey(mailboxInfo.getId()))
          // .filter(e -> !e.getKeywords().containsKey(Keyword.SEEN))
          .count())
      .totalThreads(
        emails.values().stream()
          .filter(e -> e.getMailboxIds().containsKey(mailboxInfo.getId()))
          .map(Email::getThreadId)
          .distinct()
          .count())
      .unreadThreads(
        emails.values().stream()
          .filter(e -> e.getMailboxIds().containsKey(mailboxInfo.getId()))
          // .filter(e -> !e.getKeywords().containsKey(Keyword.SEEN))
          .map(Email::getThreadId)
          .distinct()
          .count())
      .build();
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

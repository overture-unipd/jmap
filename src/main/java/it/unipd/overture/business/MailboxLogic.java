package it.unipd.overture.business;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.inject.Inject;

import it.unipd.overture.ports.out.MailboxPort;
import rs.ltt.jmap.common.Request;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.Role;
import rs.ltt.jmap.common.entity.SetError;
import rs.ltt.jmap.common.entity.SetErrorType;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.mailbox.ChangesMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.GetMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.SetMailboxMethodCall;
import rs.ltt.jmap.common.method.error.CannotCalculateChangesMethodErrorResponse;
import rs.ltt.jmap.common.method.error.InvalidResultReferenceMethodErrorResponse;
import rs.ltt.jmap.common.method.error.StateMismatchMethodErrorResponse;
import rs.ltt.jmap.common.method.response.mailbox.ChangesMailboxMethodResponse;
import rs.ltt.jmap.common.method.response.mailbox.GetMailboxMethodResponse;
import rs.ltt.jmap.common.method.response.mailbox.SetMailboxMethodResponse;
import rs.ltt.jmap.mock.server.Changes;
import rs.ltt.jmap.mock.server.ResultReferenceResolver;
import rs.ltt.jmap.mock.server.util.FuzzyRoleParser;

public class MailboxLogic {
  Gson gson;
  MailboxPort mailbox;
  
  @Inject
  MailboxLogic(Gson gson, MailboxPort mailbox) {
    this.gson = gson;
    this.mailbox = mailbox;
  }

  /*
  public void setupInbox() {
    MailboxInfo m = new MailboxInfo(UUID.randomUUID().toString(), "Inbox", Role.INBOX, true);
    insertMailbox(m.getId(), m);
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

  private void insertMailbox(String id, MailboxInfo mailbox) {
    db.insertInTable("mailbox", gson.toJson(mailbox));
  }
  */

  public MethodResponse[] changes(ChangesMailboxMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
    /*
    final String since = methodCall.getSinceState();
    if (since != null && since.equals(getState())) {
      return new MethodResponse[] {
        ChangesMailboxMethodResponse.builder()
          .oldState(getState())
          .newState(getState())
          .updated(new String[0])
          .created(new String[0])
          .destroyed(new String[0])
          .updatedProperties(new String[0])
          .build()
      };
    } else {
      final Update update = getAccumulatedUpdateSince(since);
      if (update == null) {
        return new MethodResponse[] {new CannotCalculateChangesMethodErrorResponse()};
      } else {
        final Changes changes = update.getChangesFor(MailboxHandler.class);
        return new MethodResponse[] {
          ChangesMailboxMethodResponse.builder()
            .oldState(since)
            .newState(update.getNewVersion())
            .updated(changes.updated)
            .created(changes.created)
            .destroyed(new String[0])
            .hasMoreChanges(!update.getNewVersion().equals(getState()))
            .build()
        };
      }
    }
    */
  }

  public MethodResponse[] get(GetMailboxMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
    /*
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
    Stream<MailboxHandler> mailboxStream = getMailboxes().values().stream().map(this::toMailbox);
    return new MethodResponse[] {
      GetMailboxMethodResponse.builder()
        .list(
          mailboxStream
            .filter(m -> ids == null || ids.contains(m.getId()))
            .toArray(MailboxHandler[]::new))
        .state(getState())
        .accountId(accountid)
        .notFound(new String[0])
        .build()
    };
    */
  }

  /*
  private MailboxHandler toMailbox(MailboxInfo mailboxInfo) {
    var emails = getEmails();
    return MailboxHandler.builder()
      .id(mailboxInfo.getId())
      .name(mailboxInfo.getName())
      .role(mailboxInfo.getRole())
      .isSubscribed(mailboxInfo.getIsSubscribed())
      // .myRights(new MailboxRights())
      .sortOrder(0L)
      .totalEmails(
        emails.values().stream()
          .filter(e -> e.getMailboxIds().containsKey(mailboxInfo.getId()))
          .count())
      .unreadEmails(
        emails.values().stream()
          .filter(e -> e.getMailboxIds().containsKey(mailboxInfo.getId()))
          .filter(e -> (e.getKeywords() == null || !e.getKeywords().containsKey(Keyword.SEEN)))
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
          .filter(e -> (e.getKeywords() == null || !e.getKeywords().containsKey(Keyword.SEEN)))
          .map(Email::getThreadId)
          .distinct()
          .count())
      .build();
  }
  */

  public MethodResponse[] set(SetMailboxMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
    /*
    final String ifInState = methodCall.getIfInState();
    final SetMailboxMethodResponse.SetMailboxMethodResponseBuilder responseBuilder =
        SetMailboxMethodResponse.builder();
    final Map<String, MailboxHandler> create = methodCall.getCreate();
    final Map<String, Map<String, Object>> update = methodCall.getUpdate();
    final String oldState = getState();
    if (ifInState != null) {
      if (!ifInState.equals(oldState)) {
        return new MethodResponse[] {new StateMismatchMethodErrorResponse()};
      }
    }
    if (create != null && create.size() > 0) {
      processCreateMailbox(create, responseBuilder);
    }
    if (update != null && update.size() > 0) {
      processUpdateMailbox(update, responseBuilder, previousResponses);
    }
    incrementState();
    final SetMailboxMethodResponse setMailboxResponse = responseBuilder.build();
    insertUpdate(oldState, Update.of(setMailboxResponse, getState()));
    return new MethodResponse[] {setMailboxResponse};
    */
  }

  /*
  private void processCreateMailbox(
      final Map<String, MailboxHandler> create,
      final SetMailboxMethodResponse.SetMailboxMethodResponseBuilder responseBuilder) {
    for (Map.Entry<String, MailboxHandler> entry : create.entrySet()) {
      final String createId = entry.getKey();
      final MailboxHandler mailbox = entry.getValue();
      final String name = mailbox.getName();
      if (getMailboxes().values().stream()
          .anyMatch(mailboxInfo -> mailboxInfo.getName().equals(name))) {
        responseBuilder.notCreated(
            createId,
            new SetError(
                SetErrorType.INVALID_PROPERTIES,
                "A mailbox with the name " + name + " already exists"));
        continue;
      }
      final String id = UUID.randomUUID().toString();
      final MailboxInfo mailboxInfo = new MailboxInfo(id, name, mailbox.getRole(), true);
      insertMailbox(id, mailboxInfo);
      responseBuilder.created(createId, toMailbox(mailboxInfo));
    }
  }

  private void processUpdateMailbox(
      Map<String, Map<String, Object>> update,
      SetMailboxMethodResponse.SetMailboxMethodResponseBuilder responseBuilder,
      ListMultimap<String, Response.Invocation> previousResponses) {
    for (final Map.Entry<String, Map<String, Object>> entry : update.entrySet()) {
      final String id = entry.getKey();
      try {
        final MailboxInfo modifiedMailbox =
            patchMailbox(id, entry.getValue(), previousResponses);
        responseBuilder.updated(id, toMailbox(modifiedMailbox));
        insertMailbox(modifiedMailbox.getId(), modifiedMailbox);
      } catch (final IllegalArgumentException e) {
        responseBuilder.notUpdated(
            id, new SetError(SetErrorType.INVALID_PROPERTIES, e.getMessage()));
      }
    }
  }

  private MailboxInfo patchMailbox(
      final String id,
      final Map<String, Object> patches,
      ListMultimap<String, Response.Invocation> previousResponses) {
    final MailboxInfo currentMailbox = getMailboxes().get(id);
    for (final Map.Entry<String, Object> patch : patches.entrySet()) {
      final String fullPath = patch.getKey();
      final Object modification = patch.getValue();
      final List<String> pathParts = Splitter.on('/').splitToList(fullPath);
      final String parameter = pathParts.get(0);
      if ("role".equals(parameter)) {
        final Role role = FuzzyRoleParser.parse((String) modification);
        return new MailboxInfo(currentMailbox.getId(), currentMailbox.getName(), role, true);
      } else {
        throw new IllegalArgumentException("Unable to patch " + fullPath);
      }
    }
    return currentMailbox;
  }
  */
}

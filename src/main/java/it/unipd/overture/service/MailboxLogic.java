package it.unipd.overture.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;

import it.unipd.overture.Update;
import it.unipd.overture.port.out.EmailPort;
import it.unipd.overture.port.out.MailboxPort;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.UpdatePort;
import rs.ltt.jmap.common.Request;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.Keyword;
import rs.ltt.jmap.common.entity.Mailbox;
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
  private MailboxPort mailboxPort;
  private EmailPort emailPort;
  private StatePort statePort;
  private UpdatePort updatePort;
  
  @Inject
  MailboxLogic(MailboxPort mailboxPort, EmailPort emailPort, StatePort statePort, UpdatePort updatePort) {
    this.mailboxPort = mailboxPort;
    this.emailPort = emailPort;
    this.statePort = statePort;
    this.updatePort = updatePort;
  }

  public MethodResponse[] changes(ChangesMailboxMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    var accountid = methodCall.getAccountId();
    var state = statePort.get(accountid);
    final String since = methodCall.getSinceState();
    if (since != null && since.equals(state)) {
      return new MethodResponse[] {
        ChangesMailboxMethodResponse.builder()
          .oldState(state)
          .newState(state)
          .updated(new String[0])
          .created(new String[0])
          .destroyed(new String[0])
          .updatedProperties(new String[0])
          .build()
      };
    } else {
      final Update update = getAccumulatedUpdateSince(since, accountid);
      if (update == null) {
        return new MethodResponse[] {new CannotCalculateChangesMethodErrorResponse()};
      } else {
        final Changes changes = update.getChangesFor(Mailbox.class);
        return new MethodResponse[] {
          ChangesMailboxMethodResponse.builder()
            .oldState(since)
            .newState(update.getNewVersion())
            .updated(changes == null ? new String[0] :  changes.updated)
            .created(changes == null ? new String[0] :  changes.created)
            .destroyed(new String[0])
            .hasMoreChanges(!state.equals(update.getNewVersion()))
            .build()
        };
      }
    }
  }

  protected Update getAccumulatedUpdateSince(final String oldVersion, final String accountid) {
    final ArrayList<Update> updates = new ArrayList<>();
    for (Map.Entry<String, Update> updateEntry : updatePort.getOf(accountid).entrySet()) {
      if (updateEntry.getKey().equals(oldVersion) || updates.size() > 0) {
        updates.add(updateEntry.getValue());
      }
    }
    if (updates.isEmpty()) {
      return null;
    }
    return Update.merge(updates);
  }

  public MethodResponse[] get(GetMailboxMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    var accountid = methodCall.getAccountId();
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
    Stream<Mailbox> mailboxStream = mailboxPort.getOf(accountid).values().stream().map(m -> toMailbox(m, accountid));
    Mailbox[] found = mailboxStream.filter(m -> ids == null || ids.contains(m.getId())).toArray(Mailbox[]::new);
    String[] notFound = ids == null ? new String[0] : ids.stream().filter(i -> !Arrays.asList(found).stream().map(m -> m.getId()).collect(Collectors.toList()).contains(i)).toArray(String[]::new);
    return new MethodResponse[] {
      GetMailboxMethodResponse.builder()
        .list(found)
        .state(statePort.get(accountid))
        .accountId(accountid)
        .notFound(notFound)
        .build()
    };
  }

  protected Mailbox toMailbox(MailboxInfo mailboxInfo, String accountid) {
    var emails = emailPort.getOf(accountid);
    return Mailbox.builder()
      .id(mailboxInfo.getId())
      .name(mailboxInfo.getName())
      .role(mailboxInfo.getRole())
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

  public MethodResponse[] set(SetMailboxMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    var accountid = methodCall.getAccountId();
    final String ifInState = methodCall.getIfInState();
    final SetMailboxMethodResponse.SetMailboxMethodResponseBuilder responseBuilder =
        SetMailboxMethodResponse.builder();
    final Map<String, Mailbox> create = methodCall.getCreate();
    final Map<String, Map<String, Object>> update = methodCall.getUpdate();
    final String[] destroy = methodCall.getDestroy();
    final String oldState = statePort.get(accountid);
    if (ifInState != null) {
      if (!ifInState.equals(oldState)) {
        return new MethodResponse[] {new StateMismatchMethodErrorResponse()};
      }
    }
    if (destroy != null && destroy.length > 0) {
      for (final String m : destroy) {
        mailboxPort.delete(m);
      }
    }
    if (create != null && create.size() > 0) {
      processCreateMailbox(create, responseBuilder, accountid);
    }
    if (update != null && update.size() > 0) {
      processUpdateMailbox(update, responseBuilder, previousResponses, accountid);
    }
    statePort.increment(accountid);
    final SetMailboxMethodResponse setMailboxResponse = responseBuilder.build();
    updatePort.insert(accountid, oldState, Update.of(setMailboxResponse, statePort.get(accountid)));
    return new MethodResponse[] {setMailboxResponse};
  }

  protected void processCreateMailbox(
      final Map<String, Mailbox> create,
      final SetMailboxMethodResponse.SetMailboxMethodResponseBuilder responseBuilder,
      final String accountid
      ) {
    for (Map.Entry<String, Mailbox> entry : create.entrySet()) {
      final String createId = entry.getKey();
      final Mailbox mailbox = entry.getValue();
      final String name = mailbox.getName();
      if (mailboxPort.getOf(accountid).values().stream()
          .anyMatch(mailboxInfo -> mailboxInfo.getName().equals(name))) {
        responseBuilder.notCreated(
            createId,
            new SetError(
                SetErrorType.INVALID_PROPERTIES,
                "A mailbox with the name " + name + " already exists"));
        continue;
      }
      if (name == null || name.equals("")) {
        responseBuilder.notCreated(
            createId,
            new SetError(
                SetErrorType.INVALID_PROPERTIES,
                "A mailbox with the name \"\" or null cannot be created"));
        continue;
      }
      final String id = UUID.randomUUID().toString();
      final MailboxInfo mailboxInfo = new MailboxInfo(id, name, mailbox.getRole());
      mailboxPort.insert(accountid, mailboxInfo);
      responseBuilder.created(createId, toMailbox(mailboxInfo, accountid));
    }
  }

  protected void processUpdateMailbox(
      Map<String, Map<String, Object>> update,
      SetMailboxMethodResponse.SetMailboxMethodResponseBuilder responseBuilder,
      ListMultimap<String, Response.Invocation> previousResponses,
      String accountid
      ) {
    for (final Map.Entry<String, Map<String, Object>> entry : update.entrySet()) {
      final String id = entry.getKey();
      try {
        final MailboxInfo modifiedMailbox =
            patchMailbox(id, entry.getValue(), previousResponses, accountid);
        responseBuilder.updated(id, toMailbox(modifiedMailbox, accountid));
        mailboxPort.insert(accountid, modifiedMailbox);
      } catch (final IllegalArgumentException e) {
        responseBuilder.notUpdated(
            id, new SetError(SetErrorType.INVALID_PROPERTIES, e.getMessage()));
      }
    }
  }

  protected MailboxInfo patchMailbox(
      final String id,
      final Map<String, Object> patches,
      ListMultimap<String, Response.Invocation> previousResponses,
      String accountid
      ) {
    final MailboxInfo currentMailbox = mailboxPort.getOf(accountid).get(id);
    for (final Map.Entry<String, Object> patch : patches.entrySet()) {
      final String fullPath = patch.getKey();
      final Object modification = patch.getValue();
      final List<String> pathParts = Splitter.on('/').splitToList(fullPath);
      final String parameter = pathParts.get(0);
      if ("role".equals(parameter)) {
        final Role role = FuzzyRoleParser.parse((String) modification);
        return new MailboxInfo(currentMailbox.getId(), currentMailbox.getName(), role);
      } else {
        throw new IllegalArgumentException("Unable to patch " + fullPath);
      }
    }
    return currentMailbox;
  }
}

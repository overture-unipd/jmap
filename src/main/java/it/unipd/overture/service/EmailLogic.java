package it.unipd.overture.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Function;
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
import rs.ltt.jmap.common.entity.Attachment;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.EmailBodyPart;
import rs.ltt.jmap.common.entity.EmailBodyValue;
import rs.ltt.jmap.common.entity.SetError;
import rs.ltt.jmap.common.entity.SetErrorType;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;
import rs.ltt.jmap.common.entity.filter.Filter;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.email.ChangesEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.GetEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.QueryEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.SetEmailMethodCall;
import rs.ltt.jmap.common.method.error.AnchorNotFoundMethodErrorResponse;
import rs.ltt.jmap.common.method.error.CannotCalculateChangesMethodErrorResponse;
import rs.ltt.jmap.common.method.error.InvalidResultReferenceMethodErrorResponse;
import rs.ltt.jmap.common.method.error.StateMismatchMethodErrorResponse;
import rs.ltt.jmap.common.method.response.email.ChangesEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.GetEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.QueryEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.SetEmailMethodResponse;
import rs.ltt.jmap.mock.server.Changes;
import rs.ltt.jmap.mock.server.CreationIdResolver;
import rs.ltt.jmap.mock.server.ResultReferenceResolver;

public class EmailLogic {
  private EmailPort emailPort;
  private MailboxPort mailboxPort;
  private StatePort statePort;
  private UpdatePort updatePort;

  @Inject
  EmailLogic(EmailPort emailPort, MailboxPort mailboxPort, StatePort statePort, UpdatePort updatePort) {
    this.emailPort = emailPort;
    this.mailboxPort = mailboxPort;
    this.statePort = statePort;
    this.updatePort = updatePort;
  }

  public MethodResponse[] get(GetEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
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
      ids = Arrays.asList(methodCall.getIds());
    }
    final String[] properties = methodCall.getProperties();
    Stream<Email> emailStream = ids.stream().map(emailPort::get);
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
        .accountId(accountid)
        .notFound(new String[0])
        .state(statePort.get(accountid))
        .build()
    };
  }

  public MethodResponse[] query(QueryEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    var accountid = methodCall.getAccountId();
    final Filter<Email> filter = methodCall.getFilter();
    Stream<Email> stream = emailPort.getOf(accountid).values().stream();
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
        .queryState(statePort.get(accountid))
        .total(total)
        .ids(page)
        .position((long) position)
        .build()
    };
  }

  protected static Stream<Email> applyFilter(
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

  protected static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    final Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

  public MethodResponse[] set(SetEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    final String accountid = methodCall.getAccountId();
    final String ifInState = methodCall.getIfInState();
    final Map<String, Map<String, Object>> update = methodCall.getUpdate();
    final Map<String, Email> create = methodCall.getCreate();
    final String[] destroy = methodCall.getDestroy();
    if (destroy != null && destroy.length > 0) {
      throw new IllegalStateException("Destroy have yet to be implemented");
    }
    final SetEmailMethodResponse.SetEmailMethodResponseBuilder responseBuilder =
        SetEmailMethodResponse.builder();
    final String oldState = statePort.get(accountid);
    if (ifInState != null) {
      if (!ifInState.equals(oldState)) {
        return new MethodResponse[] {new StateMismatchMethodErrorResponse()};
      }
    }
    if (update != null) {
      final List<Email> modifiedEmails = new ArrayList<>();
      for (final Map.Entry<String, Map<String, Object>> entry : update.entrySet()) {
        final String id = entry.getKey();
        try {
          final Email modifiedEmail = patchEmail(id, entry.getValue(), previousResponses);
          modifiedEmails.add(modifiedEmail);
          responseBuilder.updated(id, modifiedEmail);
        } catch (final IllegalArgumentException e) {
          responseBuilder.notUpdated(
            id, new SetError(SetErrorType.INVALID_PROPERTIES, e.getMessage()));
        }
      }
      for (final Email email : modifiedEmails) {
        emailPort.insert(accountid, email);
      }
      statePort.increment(accountid);
      final String newState = statePort.get(accountid);
      updatePort.insert(accountid, oldState, Update.updated(modifiedEmails, mailboxPort.getOf(accountid).keySet(), newState));
    }
    if (create != null && create.size() > 0) {
      processCreateEmail(create, responseBuilder, previousResponses, accountid);
    }
    return new MethodResponse[] {responseBuilder.build()};
  }

  protected Email patchEmail(
      final String id,
      final Map<String, Object> patches,
      ListMultimap<String, Response.Invocation> previousResponses) {
    final Email email = emailPort.get(id);
    Map<String, Boolean> old_keywords = email.getKeywords();
    if (old_keywords == null) {
      old_keywords = new HashMap<String, Boolean>();
    }
    final Map<String, Boolean> new_keywords = new HashMap<String, Boolean>();
    final Email.EmailBuilder emailBuilder = email.toBuilder();
    emailBuilder.clearKeywords();
    for (final Map.Entry<String, Object> patch : patches.entrySet()) {
      final String fullPath = patch.getKey();
      final Object modification = patch.getValue();
      final List<String> pathParts = Splitter.on('/').splitToList(fullPath);
      final String parameter = pathParts.get(0);
      if (parameter.equals("keywords")) {
        if (pathParts.size() == 2) {
          final String keyword = pathParts.get(1);
          final Boolean value = modification instanceof Boolean ? (Boolean) modification : false;
          new_keywords.put(keyword, value);
        } else {
          throw new IllegalArgumentException(
              "Keyword modification was not split into two parts");
        }
        old_keywords.putAll(new_keywords);
        old_keywords.keySet().removeAll(
          old_keywords.entrySet().stream()
           .filter(a->a.getValue().equals(false))
           .map(e -> e.getKey()).collect(Collectors.toList()));
        old_keywords.forEach((k, v) -> emailBuilder.keyword(k, v));
        emailBuilder.keyword("placeholder", true);
      } else if (parameter.equals("mailboxIds")) {
        if (pathParts.size() == 2 && modification instanceof Boolean) {
          final String mailboxId = pathParts.get(1);
          final Boolean value = (Boolean) modification;
          emailBuilder.mailboxId(mailboxId, value);
        } else if (modification instanceof Map) {
          final Map<String, Boolean> mailboxMap = (Map<String, Boolean>) modification;
          emailBuilder.clearMailboxIds();
          for (Map.Entry<String, Boolean> mailboxEntry : mailboxMap.entrySet()) {
            final String mailboxId =
                CreationIdResolver.resolveIfNecessary(
                    mailboxEntry.getKey(), previousResponses);
            emailBuilder.mailboxId(mailboxId, mailboxEntry.getValue());
          }
        } else {
          throw new IllegalArgumentException("Unknown patch object for path " + fullPath);
        }
      } else {
        throw new IllegalArgumentException("Unable to patch " + fullPath);
      }
    }
    return emailBuilder.build();
  }

  protected void processCreateEmail(
      Map<String, Email> create,
      SetEmailMethodResponse.SetEmailMethodResponseBuilder responseBuilder,
      ListMultimap<String, Response.Invocation> previousResponses,
      final String accountid
      ) {
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
          .blobId(id)
          .receivedAt(Instant.now())
          .bodyStructure(userSuppliedEmail.getTextBody().get(0));
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
            emailBuilder.attachment(emailBodyPart);
          } else {
            emailBuilder.attachment(attachment);
          }
        }
      }
      final Email email = emailBuilder.build();
      emailPort.insert(accountid, email);
      statePort.increment(accountid);
      responseBuilder.created(createId, email);
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

  public MethodResponse[] changes(ChangesEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    final String accountid = methodCall.getAccountId();
    final String since = methodCall.getSinceState();
    final String state = statePort.get(accountid);
    if (since != null && since.equals(state)) {
      return new MethodResponse[] {
        ChangesEmailMethodResponse.builder()
          .oldState(state)
          .newState(state)
          .updated(new String[0])
          .created(new String[0])
          .destroyed(new String[0])
          .build()
      };
    } else {
      final Update update = getAccumulatedUpdateSince(since, accountid);
      if (update == null) {
        return new MethodResponse[] {new CannotCalculateChangesMethodErrorResponse()};
      } else {
        final Changes changes = update.getChangesFor(Email.class);
        return new MethodResponse[] {
          ChangesEmailMethodResponse.builder()
            .oldState(since)
            .newState(update.getNewVersion())
            .updated(changes == null ? new String[0] : changes.updated)
            .created(changes == null ? new String[0] : changes.created)
            .destroyed(new String[0])
            .hasMoreChanges(!state.equals(update.getNewVersion()))
            .build()
        };
      }
    }
  }

  protected static EmailBodyPart injectId(final Attachment attachment) {
    return EmailBodyPart.builder()
        .blobId(UUID.randomUUID().toString())
        .charset(attachment.getCharset())
        .type(attachment.getType())
        .name(attachment.getName())
        .size(attachment.getSize())
        .build();
  }
}

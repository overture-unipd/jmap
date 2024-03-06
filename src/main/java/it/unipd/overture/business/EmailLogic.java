package it.unipd.overture.business;

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

import it.unipd.overture.ports.out.EmailPort;
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
  EmailPort emailport;

  EmailLogic(EmailPort emailport) {
    this.emailport = emailport;
  }

  public MethodResponse[] get(GetEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
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
        .accountId(accountid)
        .notFound(new String[0])
        .state(getState())
        .build()
    };
    */
  }

  public MethodResponse[] query(QueryEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
    /*
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
    */
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

  public MethodResponse[] set(SetEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
    /*
    final String ifInState = methodCall.getIfInState();
    final Map<String, Map<String, Object>> update = methodCall.getUpdate();
    final Map<String, Email> create = methodCall.getCreate();
    final String[] destroy = methodCall.getDestroy();
    if (destroy != null && destroy.length > 0) {
      throw new IllegalStateException("Destroy still have to be implemented"); // TODO
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
        updateEmail(email.getId(), email);
      }
      incrementState();
      final String newState = getState();
      insertUpdate(oldState, Update.updated(modifiedEmails, getMailboxes().keySet(), newState));
    }
    if (create != null && create.size() > 0) {
      processCreateEmail(create, responseBuilder, previousResponses);
    }
    return new MethodResponse[] {responseBuilder.build()};
    */
  }

  /*
  private Email patchEmail(
      final String id,
      final Map<String, Object> patches,
      ListMultimap<String, Response.Invocation> previousResponses) {
    final Email email = getEmails().get(id);
    Map<String, Boolean> old_keywords = email.getKeywords();
    if (old_keywords == null) {
      old_keywords = new HashMap<String, Boolean>();
    }
    final Map<String, Boolean> new_keywords = new HashMap<String, Boolean>();
    final Email.EmailBuilder emailBuilder = getEmails().get(id).toBuilder();
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
  */

  /*
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
  } */

  public MethodResponse[] changes(ChangesEmailMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
    /*
    final String since = methodCall.getSinceState();
    if (since != null && since.equals(getState())) {
      return new MethodResponse[] {
        ChangesEmailMethodResponse.builder()
          .oldState(getState())
          .newState(getState())
          .updated(new String[0])
          .created(new String[0])
          .destroyed(new String[0])
          .build()
      };
    } else {
      final Update update = getAccumulatedUpdateSince(since);
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
            .hasMoreChanges(!update.getNewVersion().equals(getState()))
            .build()
        };
      }
    }
    */
  }

  private static EmailBodyPart injectId(final Attachment attachment) {
    // TODO: vedi allegati in processCreateEmail
    return EmailBodyPart.builder()
        .blobId(UUID.randomUUID().toString())
        .charset(attachment.getCharset())
        .type(attachment.getType())
        .name(attachment.getName())
        .size(attachment.getSize())
        .build();
  }
}

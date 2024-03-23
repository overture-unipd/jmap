package it.unipd.overture.service;

import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;

import it.unipd.overture.Update;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.ThreadPort;
import it.unipd.overture.port.out.UpdatePort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import rs.ltt.jmap.common.Request;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.Thread;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.thread.ChangesThreadMethodCall;
import rs.ltt.jmap.common.method.call.thread.GetThreadMethodCall;
import rs.ltt.jmap.common.method.error.CannotCalculateChangesMethodErrorResponse;
import rs.ltt.jmap.common.method.error.InvalidResultReferenceMethodErrorResponse;
import rs.ltt.jmap.common.method.response.thread.ChangesThreadMethodResponse;
import rs.ltt.jmap.common.method.response.thread.GetThreadMethodResponse;
import rs.ltt.jmap.mock.server.Changes;
import rs.ltt.jmap.mock.server.ResultReferenceResolver;

public class ThreadLogic {
  private ThreadPort threadPort;
  private UpdatePort updatePort;
  private StatePort statePort;

  @Inject
  ThreadLogic(ThreadPort threadPort, UpdatePort updatePort, StatePort statePort) {
    this.threadPort = threadPort;
    this.updatePort = updatePort;
    this.statePort = statePort;
  }

  public MethodResponse[] changes(ChangesThreadMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    final String accountid = methodCall.getAccountId();
    final String state = statePort.get(accountid);
    final String since = methodCall.getSinceState();
    if (since != null && since.equals(state)) {
      return new MethodResponse[] {
        ChangesThreadMethodResponse.builder()
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
        final Changes changes = update.getChangesFor(Thread.class);
        return new MethodResponse[] {
          ChangesThreadMethodResponse.builder()
            .oldState(since)
            .newState(update.getNewVersion())
            .updated(changes == null ? new String[0] : changes.updated)
            .created(changes == null ? new String[0] : changes.created)
            .destroyed(new String[0])
            .hasMoreChanges(!statePort.get(accountid).equals(update.getNewVersion()))
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

  public MethodResponse[] get(GetThreadMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    final String accountid = methodCall.getAccountId();
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
    final Thread[] threads =
      ids.stream()
        .map(
          threadId ->
            Thread.builder()
              .id(threadId)
              .emailIds(threadPort.getOf(accountid, threadId))
              .build())
        .toArray(Thread[]::new);
    return new MethodResponse[] {
      GetThreadMethodResponse.builder().list(threads).state(statePort.get(accountid)).build()
    };
  }
}

package it.unipd.overture.business;

public class Thread {
  public changes(ChangesThreadMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    final String since = methodCall.getSinceState();
    if (since != null && since.equals(getState())) {
      return new MethodResponse[] {
        ChangesThreadMethodResponse.builder()
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
        final Changes changes = update.getChangesFor(Thread.class);
        return new MethodResponse[] {
          ChangesThreadMethodResponse.builder()
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
  }

  public get(GetThreadMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
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
              .emailIds(
                getEmails().values().stream()
                  .filter(
                    email ->
                      email.getThreadId()
                        .equals(
                          threadId))
                  .sorted(
                    Comparator.comparing(
                      Email
                        ::getReceivedAt))
                  .map(Email::getId)
                  .collect(Collectors.toList()))
              .build())
        .toArray(Thread[]::new);
    return new MethodResponse[] {
      GetThreadMethodResponse.builder().list(threads).state(getState()).build()
    };
  }
}

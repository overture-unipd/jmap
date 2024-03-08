package it.unipd.overture.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.inject.Inject;

import it.unipd.overture.service.EchoLogic;
import it.unipd.overture.service.EmailLogic;
import it.unipd.overture.service.EmailSubmissionLogic;
import it.unipd.overture.service.IdentityLogic;
import it.unipd.overture.service.MailboxLogic;
import it.unipd.overture.service.ThreadLogic;
import rs.ltt.jmap.common.ErrorResponse;
import rs.ltt.jmap.common.Request;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.ErrorType;
import rs.ltt.jmap.common.entity.Mailbox;
import rs.ltt.jmap.common.method.MethodCall;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.core.EchoMethodCall;
import rs.ltt.jmap.common.method.call.email.ChangesEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.GetEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.QueryEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.SetEmailMethodCall;
import rs.ltt.jmap.common.method.call.submission.SetEmailSubmissionMethodCall;
import rs.ltt.jmap.common.method.call.submission.GetEmailSubmissionMethodCall;
import rs.ltt.jmap.common.method.call.identity.GetIdentityMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.ChangesMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.GetMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.SetMailboxMethodCall;
import rs.ltt.jmap.common.method.call.thread.ChangesThreadMethodCall;
import rs.ltt.jmap.common.method.call.thread.GetThreadMethodCall;
import rs.ltt.jmap.common.method.error.UnknownMethodMethodErrorResponse;

public class MethodController {
  private Gson gson;
  private EchoLogic echo;
  private EmailLogic email;
  private EmailSubmissionLogic submission;
  private IdentityLogic identity;
  private MailboxLogic mailbox;
  private ThreadLogic thread;

  @Inject
  MethodController(
    Gson gson,
    EchoLogic echo,
    EmailLogic email,
    EmailSubmissionLogic submission,
    IdentityLogic identity,
    MailboxLogic mailbox,
    ThreadLogic thread
  ) {
    this.gson = gson;
    this.echo = echo;
    this.email = email;
    this.submission = submission;
    this.identity = identity;
    this.mailbox = mailbox;
    this.thread = thread;
  }

  String dispatch(String in) {
    //  System.out.println(">>>>>\n"+ in + "\n\n"); // TODO: replace with logs
    var request = gson.fromJson(in, Request.class);
    final var methodCalls = request.getMethodCalls();
    final var using = request.getUsing();
    if (using == null || methodCalls == null) {
      return gson.toJson(new ErrorResponse(ErrorType.NOT_REQUEST, 400));
    }
    final ArrayListMultimap<String, Response.Invocation> response = ArrayListMultimap.create();
    for (final Request.Invocation invocation : methodCalls) {
      final String id = invocation.getId();
      final MethodCall methodCall = invocation.getMethodCall();
      for (MethodResponse methodResponse :
          pick(methodCall, ImmutableListMultimap.copyOf(response))) {
        response.put(id, new Response.Invocation(methodResponse, id));
      }
    }
    var out = gson.toJson(new Response(response.values().toArray(new Response.Invocation[0]), ""));
    // System.out.println("<<<<<\n"+ out + "\n\n");
    return gson.toJson(out);
  }

  private MethodResponse[] pick (
    MethodCall methodCall,
    ListMultimap<String, Response.Invocation> prevResponses
  ) {
    return switch(methodCall) {
      case EchoMethodCall call -> {
        yield echo.echo(call, prevResponses);
      }
      case GetIdentityMethodCall call -> {
        yield identity.get(call, prevResponses);
      }
      case GetEmailMethodCall call -> {
        yield email.get(call, prevResponses);
      }
      case ChangesEmailMethodCall call -> {
        yield email.changes(call, prevResponses);
      }
      case QueryEmailMethodCall call -> {
        yield email.query(call, prevResponses);
      }
      case SetEmailMethodCall call -> {
        yield email.set(call, prevResponses);
      }
      case SetEmailSubmissionMethodCall call -> {
        yield submission.set(call, prevResponses);
      }
      case GetEmailSubmissionMethodCall call -> {
        yield submission.get(call, prevResponses);
      }
      case GetMailboxMethodCall call -> {
        yield mailbox.get(call, prevResponses);
      }
      case ChangesMailboxMethodCall call -> {
        yield mailbox.changes(call, prevResponses);
      }
      case SetMailboxMethodCall call -> {
        yield mailbox.set(call, prevResponses);
      }
      case GetThreadMethodCall call -> {
        yield thread.get(call, prevResponses);
      }
      case ChangesThreadMethodCall call -> {
        yield thread.changes(call, prevResponses);
      }
      default -> {
        yield new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
      }
    };
  }
}

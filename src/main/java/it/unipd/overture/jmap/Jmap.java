package it.unipd.overture.jmap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import rs.ltt.jmap.common.*;
import rs.ltt.jmap.common.entity.*;
import rs.ltt.jmap.common.method.*;
import rs.ltt.jmap.common.method.call.core.*;
import rs.ltt.jmap.common.method.call.email.*;
import rs.ltt.jmap.common.method.call.identity.*;
import rs.ltt.jmap.common.method.call.mailbox.*;
import rs.ltt.jmap.common.method.call.snippet.GetSearchSnippetsMethodCall;
import rs.ltt.jmap.common.method.call.submission.*;
import rs.ltt.jmap.common.method.call.thread.*;
import rs.ltt.jmap.common.method.call.vacation.*;
import rs.ltt.jmap.common.method.error.*;
import rs.ltt.jmap.common.method.response.core.*;
import rs.ltt.jmap.gson.JmapAdapters;

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

  private String getSessionState() {
    return new Database().getAccountState(accountid);
  }

  public String dispatch() {
    // should be in a `try`
    var jmapRequest = gson.fromJson(request, Request.class);
    final GenericResponse response = dispatch(jmapRequest);
    if (response instanceof ErrorResponse) {
      return gson.toJson(response); // should give an error 400 along with the response
    }
    return gson.toJson(response);
  }

  protected GenericResponse dispatch(final Request request) {
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
        response.values().toArray(new Response.Invocation[0]), getSessionState());
  }

  protected MethodResponse[] dispatch(
      final MethodCall methodCall,
      final ListMultimap<String, Response.Invocation> previousResponses) {
    /** jmap-core */
    if (methodCall instanceof EchoMethodCall) {
      return execute((EchoMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetPushSubscriptionMethodCall) {
      return execute((GetPushSubscriptionMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof SetPushSubscriptionMethodCall) {
      return execute((SetPushSubscriptionMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Email */
    if (methodCall instanceof ChangesEmailMethodCall) {
      return execute((ChangesEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof CopyEmailMethodCall) {
      return execute((CopyEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetEmailMethodCall) {
      return execute((GetEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof ImportEmailMethodCall) {
      return execute((ImportEmailMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof ParseEmailMethodCall) {
      return execute((ParseEmailMethodCall) methodCall, previousResponses);
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
    if (methodCall instanceof ChangesIdentityMethodCall) {
      return execute((ChangesIdentityMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetIdentityMethodCall) {
      return execute((GetIdentityMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof SetIdentityMethodCall) {
      return execute((SetIdentityMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Mailbox */
    if (methodCall instanceof ChangesMailboxMethodCall) {
      return execute((ChangesMailboxMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetMailboxMethodCall) {
      return execute((GetMailboxMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof QueryChangesMailboxMethodCall) {
      return execute((QueryChangesMailboxMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof QueryMailboxMethodCall) {
      return execute((QueryMailboxMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof SetMailboxMethodCall) {
      return execute((SetMailboxMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Snippet */
    if (methodCall instanceof GetSearchSnippetsMethodCall) {
      return execute((GetSearchSnippetsMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Submission */
    if (methodCall instanceof ChangesEmailSubmissionMethodCall) {
      return execute((ChangesEmailSubmissionMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetEmailSubmissionMethodCall) {
      return execute((GetEmailSubmissionMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof QueryChangesEmailSubmissionMethodCall) {
      return execute((QueryChangesEmailSubmissionMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof QueryEmailSubmissionMethodCall) {
      return execute((QueryEmailSubmissionMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof SetEmailSubmissionMethodCall) {
      return execute((SetEmailSubmissionMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Thread */
    if (methodCall instanceof ChangesThreadMethodCall) {
      return execute((ChangesThreadMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof GetThreadMethodCall) {
      return execute((GetThreadMethodCall) methodCall, previousResponses);
    }

    /** jmap-mail / Vacation */
    if (methodCall instanceof GetVacationResponseMethodCall) {
      return execute((GetVacationResponseMethodCall) methodCall, previousResponses);
    }
    if (methodCall instanceof SetVacationResponseMethodCall) {
      return execute((SetVacationResponseMethodCall) methodCall, previousResponses);
    }

    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      EchoMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {
      EchoMethodResponse.builder().libraryName(methodCall.getLibraryName()).build()
    };
  }

  protected MethodResponse[] execute(
      SetPushSubscriptionMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetPushSubscriptionMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      ChangesEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      CopyEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      ImportEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      ParseEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      QueryChangesEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      QueryEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      SetEmailMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      ChangesIdentityMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetIdentityMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      SetIdentityMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      ChangesMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      QueryChangesMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      QueryMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      SetMailboxMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetSearchSnippetsMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      ChangesEmailSubmissionMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetEmailSubmissionMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      QueryChangesEmailSubmissionMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      QueryEmailSubmissionMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      SetEmailSubmissionMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      ChangesThreadMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetThreadMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      GetVacationResponseMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }

  protected MethodResponse[] execute(
      SetVacationResponseMethodCall methodCall,
      ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {new UnknownMethodMethodErrorResponse()};
  }
}

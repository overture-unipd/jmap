package it.unipd.overture.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;

import it.unipd.overture.service.EchoLogic;
import it.unipd.overture.service.EmailLogic;
import it.unipd.overture.service.EmailSubmissionLogic;
import it.unipd.overture.service.IdentityLogic;
import it.unipd.overture.service.MailboxLogic;
import it.unipd.overture.service.ThreadLogic;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import rs.ltt.jmap.common.Request;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.method.MethodCall;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.core.EchoMethodCall;
import rs.ltt.jmap.common.method.call.email.ChangesEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.GetEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.QueryEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.SetEmailMethodCall;
import rs.ltt.jmap.common.method.call.submission.SetEmailSubmissionMethodCall;
import rs.ltt.jmap.common.method.call.identity.GetIdentityMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.ChangesMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.GetMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.SetMailboxMethodCall;
import rs.ltt.jmap.common.method.call.thread.ChangesThreadMethodCall;
import rs.ltt.jmap.common.method.call.thread.GetThreadMethodCall;

import rs.ltt.jmap.common.method.response.core.EchoMethodResponse;
import rs.ltt.jmap.common.method.response.identity.GetIdentityMethodResponse;
import rs.ltt.jmap.common.method.response.email.GetEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.ChangesEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.QueryEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.SetEmailMethodResponse;
import rs.ltt.jmap.common.method.response.submission.SetEmailSubmissionMethodResponse;
import rs.ltt.jmap.common.method.response.mailbox.GetMailboxMethodResponse;
import rs.ltt.jmap.common.method.response.mailbox.ChangesMailboxMethodResponse;
import rs.ltt.jmap.common.method.response.mailbox.SetMailboxMethodResponse;
import rs.ltt.jmap.common.method.response.thread.GetThreadMethodResponse;
import rs.ltt.jmap.common.method.response.thread.ChangesThreadMethodResponse;

public class MethodControllerTest {
    @Mock Gson gson;
    @Mock EchoLogic echo;
    @Mock EmailLogic email;
    @Mock EmailSubmissionLogic submission;
    @Mock IdentityLogic identity;
    @Mock MailboxLogic mailbox;
    @Mock ThreadLogic thread;
    @Mock Request request;
    @Mock Request.Invocation invocation;
    @Mock Request.Invocation.ResultReference idsReference;
    @Mock MethodCall methodCall;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    
    @Test
    public void testDispatch() {
      String in = "{\"response\": \"response\"}";
      when(gson.fromJson(in, Request.class)).thenReturn(request);
      when(gson.toJson(any(Response.class))).thenReturn("{\"response\": \"response\"}");
      when(gson.toJson(any(String.class))).thenReturn("{\"response\": \"response\"}");
      when(request.getMethodCalls()).thenReturn(new Request.Invocation[] {invocation});
      when(request.getUsing()).thenReturn(new String[]{"urn:ietf:params:jmap:core"});
      when(invocation.getId()).thenReturn("id");
      when(invocation.getMethodCall()).thenReturn(methodCall);
      MethodController methodController = new MethodController(gson, echo, email, submission, identity, mailbox, thread);
      String response = methodController.dispatch(in);
      Assertions.assertNotNull(response);
      Assertions.assertEquals("{\"response\": \"response\"}", response);
      when(request.getMethodCalls()).thenReturn(null);
      when(request.getUsing()).thenReturn(null);
      response = methodController.dispatch(in);
    }

    @Test
    public void testDispatchFailed() {
      String in = "{\"response\": \"response\"}";
      when(gson.fromJson(in, Request.class)).thenReturn(request);
      when(gson.toJson(any(Response.class))).thenReturn("{\"response\": \"response\"}");
      when(gson.toJson(any(String.class))).thenReturn("{\"response\": \"response\"}");
      when(request.getMethodCalls()).thenReturn(new Request.Invocation[] {invocation});
      when(request.getUsing()).thenReturn(new String[]{"urn:ietf:params:jmap:mail"});
      when(invocation.getId()).thenReturn("id");
      when(invocation.getMethodCall()).thenReturn(methodCall);
      MethodController methodController = new MethodController(gson, echo, email, submission, identity, mailbox, thread);
      String response = methodController.dispatch(in);
      Assertions.assertNull(response);
    }

    @Test
    public void testDispatchFailed2() {
      String in = "{\"response\": \"response\"}";
      when(gson.fromJson(in, Request.class)).thenReturn(request);
      when(gson.toJson(any(Response.class))).thenReturn("{\"response\": \"response\"}");
      when(gson.toJson(any(String.class))).thenReturn("{\"response\": \"response\"}");
      when(request.getMethodCalls()).thenReturn(null);
      when(request.getUsing()).thenReturn(null);
      when(invocation.getId()).thenReturn("id");
      when(invocation.getMethodCall()).thenReturn(methodCall);
      MethodController methodController = new MethodController(gson, echo, email, submission, identity, mailbox, thread);
      String response = methodController.dispatch(in);
      Assertions.assertNull(response);
    }

    @Test
    public void pickTest() {
        String libraryName = "test";
        MethodController methodController = new MethodController(gson, echo, email, submission, identity, mailbox, thread);
        ListMultimap<String, Response.Invocation> previousResponses = ArrayListMultimap.create();
        when(echo.echo(Mockito.any(EchoMethodCall.class))).thenReturn(new MethodResponse[] {EchoMethodResponse.builder().libraryName(libraryName).build()});
        when(identity.get(Mockito.any(GetIdentityMethodCall.class))).thenReturn(new MethodResponse[] {GetIdentityMethodResponse.builder().build()});
        when(email.get(Mockito.any(GetEmailMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {GetEmailMethodResponse.builder().build()});
        when(email.changes(Mockito.any(ChangesEmailMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {ChangesEmailMethodResponse.builder().build()});
        when(email.query(Mockito.any(QueryEmailMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {QueryEmailMethodResponse.builder().build()});
        when(email.set(Mockito.any(SetEmailMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {SetEmailMethodResponse.builder().build()});
        when(submission.set(Mockito.any(SetEmailSubmissionMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {SetEmailSubmissionMethodResponse.builder().build()});
        when(mailbox.get(Mockito.any(GetMailboxMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {GetMailboxMethodResponse.builder().build()});
        when(mailbox.changes(Mockito.any(ChangesMailboxMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {ChangesMailboxMethodResponse.builder().build()});
        when(mailbox.set(Mockito.any(SetMailboxMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {SetMailboxMethodResponse.builder().build()});
        when(thread.get(Mockito.any(GetThreadMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {GetThreadMethodResponse.builder().build()});
        when(thread.changes(Mockito.any(ChangesThreadMethodCall.class), Mockito.any())).thenReturn(new MethodResponse[] {ChangesThreadMethodResponse.builder().build()});
        MethodResponse[] actualResponse = methodController.pick(new EchoMethodCall(libraryName), previousResponses);
        String actualLibraryName = ((EchoMethodResponse) actualResponse[0]).getLibraryName();
        Assertions.assertEquals(libraryName, actualLibraryName);
        methodController.pick(new GetIdentityMethodCall("accountId", new String[]{""}, new String[]{""}, null), previousResponses);
        methodController.pick(new GetEmailMethodCall("accountId", new String[]{""}, new String[]{""}, null, new String[]{""}, false, false, false, 1024L), previousResponses);
        methodController.pick(new ChangesEmailMethodCall("accountId", "sinceState", 1024L), previousResponses);
        methodController.pick(new QueryEmailMethodCall("accountId", null, null, 1024L, "anchor", 1024L, 1024L, false, false), previousResponses);
        methodController.pick(new SetEmailMethodCall("accountId", "inState", null, null, new String[]{""}, null), previousResponses);
        methodController.pick(new SetEmailSubmissionMethodCall("accountId", "inState", null, null, new String[]{""}, null, null, null), previousResponses);
        methodController.pick(new GetMailboxMethodCall("accountId", new String[]{""}, new String[]{""}, null, null), previousResponses);
        methodController.pick(new ChangesMailboxMethodCall("accountId", "sinceState", 1024L), previousResponses);
        methodController.pick(new SetMailboxMethodCall("accountId", "inState", null, null, new String[]{""}, null, false), previousResponses);
        methodController.pick(new GetThreadMethodCall("accountId", new String[]{""}, new String[]{""}, null), previousResponses);
        methodController.pick(new ChangesThreadMethodCall("accountId", "sinceState", 1024L), previousResponses);
    }
   
}

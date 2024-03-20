package it.unipd.overture.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import it.unipd.overture.Update;
import it.unipd.overture.port.out.EmailPort;
import it.unipd.overture.port.out.MailboxPort;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.UpdatePort;
import it.unipd.overture.service.EmailLogic;
import rs.ltt.jmap.common.Request;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.common.entity.Attachment;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.EmailBodyPart;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.email.ChangesEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.GetEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.QueryEmailMethodCall;
import rs.ltt.jmap.common.method.call.email.SetEmailMethodCall;
import rs.ltt.jmap.common.method.response.email.ChangesEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.GetEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.QueryEmailMethodResponse;
import rs.ltt.jmap.common.method.response.email.SetEmailMethodResponse;
import rs.ltt.jmap.mock.server.Changes;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmailLogicTest {
    @Mock EmailPort emailPort;
    @Mock MailboxPort mailboxPort;
    @Mock StatePort statePort;
    @Mock UpdatePort updatePort;
    @Mock Email email;
    @Mock Email.EmailBuilder emailBuilder;
    @Mock EmailBodyPart emailBodyPart;
    @Mock EmailFilterCondition emailFilterCondition;
    @Mock Update oldUpdate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAccumulatedUpdateSinceTest() {
      String accountId = "accountId";
      String oldVersion = "oldVersion";
      Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
      EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
      Map<String, Update> updateMap = new HashMap<>();
      updateMap.put(oldVersion, oldUpdate);

      when(updatePort.getOf(accountId)).thenReturn(updateMap);
      when(oldUpdate.getChanges()).thenReturn(changesMap);

      Update result = emailLogic.getAccumulatedUpdateSince(oldVersion, accountId);
      Assertions.assertNotNull(result);
    }

    @Test
    public void getAccumulatedUpdateSinceTestFailed() {
      String accountId = "accountId";
      String oldVersion = "oldVersion";
      String newVersion = "newVersion";
      Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
      EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
      Map<String, Update> updateMap = new HashMap<>();
      updateMap.put(oldVersion, oldUpdate);

      when(updatePort.getOf(accountId)).thenReturn(updateMap);
      when(oldUpdate.getChanges()).thenReturn(changesMap);

      Update result = emailLogic.getAccumulatedUpdateSince(newVersion, accountId);
      Assertions.assertNull(result);
    }

    @Test
    public void getTest() {
        String accountId = "accountId";
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        ListMultimap<String, Response.Invocation> previousResponses = ArrayListMultimap.create();

        when(emailPort.get(Mockito.any())).thenReturn(email);

        MethodResponse[] actualResponse = emailLogic.get(new GetEmailMethodCall(accountId, new String[]{""}, new String[]{""}, null, new String[]{""}, false, false, false, 1024L), previousResponses);
        String actualAccountId = ((GetEmailMethodResponse) actualResponse[0]).getAccountId();
        Assertions.assertTrue(actualResponse[0] instanceof GetEmailMethodResponse);
        Assertions.assertEquals(accountId, actualAccountId);
    }

    @Test
    public void getTest2() {
        String accountId = "accountId";
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        ListMultimap<String, Response.Invocation> previousResponses = ArrayListMultimap.create();

        when(emailPort.get(Mockito.any())).thenReturn(email);

        MethodResponse[] actualResponse = emailLogic.get(new GetEmailMethodCall(accountId, new String[]{""}, Email.Properties.THREAD_ID, null, new String[]{""}, false, false, false, 1024L), previousResponses);
        String actualAccountId = ((GetEmailMethodResponse) actualResponse[0]).getAccountId();
        Assertions.assertTrue(actualResponse[0] instanceof GetEmailMethodResponse);
        Assertions.assertEquals(accountId, actualAccountId);
    }

    @Test
    public void getTest3() {
        String accountId = "accountId";
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        ListMultimap<String, Response.Invocation> previousResponses = ArrayListMultimap.create();

        when(emailPort.get(Mockito.any())).thenReturn(email);

        MethodResponse[] actualResponse = emailLogic.get(new GetEmailMethodCall(accountId, new String[]{""}, Email.Properties.MUTABLE, null, new String[]{""}, false, false, false, 1024L), previousResponses);
        String actualAccountId = ((GetEmailMethodResponse) actualResponse[0]).getAccountId();
        Assertions.assertTrue(actualResponse[0] instanceof GetEmailMethodResponse);
        Assertions.assertEquals(accountId, actualAccountId);
    }

    @Test
    public void queryTest() {
        QueryEmailMethodCall methodCall = mock(QueryEmailMethodCall.class);
        ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);

        when(methodCall.getAccountId()).thenReturn("accountId");
        when(methodCall.getFilter()).thenReturn(emailFilterCondition);
        when(methodCall.getCollapseThreads()).thenReturn(true);
        when(methodCall.getAnchor()).thenReturn(null);
        when(methodCall.getAnchorOffset()).thenReturn(1L);
        when(methodCall.getPosition()).thenReturn(0L);
        when(methodCall.getLimit()).thenReturn(10L);
        when(methodCall.getCalculateTotal()).thenReturn(true);
        when(statePort.get(Mockito.any())).thenReturn("state");

        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        MethodResponse[] methodResponses = emailLogic.query(methodCall, previousResponses);
        Assertions.assertNotNull(methodResponses);
        Assertions.assertEquals(1, methodResponses.length);
        Assertions.assertTrue(methodResponses[0] instanceof QueryEmailMethodResponse);
        QueryEmailMethodResponse queryEmailMethodResponse = (QueryEmailMethodResponse) methodResponses[0];
        Assertions.assertFalse(queryEmailMethodResponse.isCanCalculateChanges());
        Assertions.assertNotNull(queryEmailMethodResponse.getQueryState());
        Assertions.assertNotNull(queryEmailMethodResponse.getTotal());
        Assertions.assertNotNull(queryEmailMethodResponse.getIds());
        Assertions.assertEquals(0, queryEmailMethodResponse.getPosition());
    }

    @Test
    public void queryTestFailed() {
        QueryEmailMethodCall methodCall = mock(QueryEmailMethodCall.class);
        ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);

        when(methodCall.getAccountId()).thenReturn("accountId");
        when(methodCall.getFilter()).thenReturn(emailFilterCondition);
        when(methodCall.getCollapseThreads()).thenReturn(true);
        when(methodCall.getAnchor()).thenReturn("anchor");
        when(methodCall.getAnchorOffset()).thenReturn(1L);
        when(methodCall.getPosition()).thenReturn(0L);
        when(methodCall.getLimit()).thenReturn(10L);
        when(methodCall.getCalculateTotal()).thenReturn(true);
        when(statePort.get(Mockito.any())).thenReturn("state");
        
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        MethodResponse[] methodResponses = emailLogic.query(methodCall, previousResponses);
        Assertions.assertNotNull(methodResponses);
        Assertions.assertEquals(1, methodResponses.length);
        Assertions.assertFalse(methodResponses[0] instanceof QueryEmailMethodResponse);
    }

    @Test
    public void queryTestFailed2() {
        QueryEmailMethodCall methodCall = mock(QueryEmailMethodCall.class);
        ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);

        when(methodCall.getAccountId()).thenReturn("accountId");
        when(methodCall.getFilter()).thenReturn(emailFilterCondition);
        when(methodCall.getCollapseThreads()).thenReturn(true);
        when(methodCall.getAnchor()).thenReturn("anchor");
        when(methodCall.getAnchorOffset()).thenReturn(1L);
        when(methodCall.getPosition()).thenReturn(0L);
        when(methodCall.getLimit()).thenReturn(10L);
        when(methodCall.getCalculateTotal()).thenReturn(true);
        when(statePort.get(Mockito.any())).thenReturn("state");
        when(emailFilterCondition.getInMailbox()).thenReturn("mailboxId");
        when(emailFilterCondition.getHeader()).thenReturn(new String[] {"Autocrypt-Setup-Message","mailboxId"});
        
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        MethodResponse[] methodResponses = emailLogic.query(methodCall, previousResponses);
        Assertions.assertNotNull(methodResponses);
        Assertions.assertEquals(1, methodResponses.length);
        Assertions.assertFalse(methodResponses[0] instanceof QueryEmailMethodResponse);
    }

    @Test
    public void setTest() {
        ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
        SetEmailMethodCall methodCall = new SetEmailMethodCall("accountId", "testState", null, null, null, null);
        when(statePort.get(any())).thenReturn("testState");
        when(emailPort.get(Mockito.any())).thenReturn(email);
        when(email.toBuilder()).thenReturn(emailBuilder);
        List<EmailBodyPart> emailBodyParts = new LinkedList<>();
        emailBodyParts.add(emailBodyPart);
        when(email.getTextBody()).thenReturn(emailBodyParts);
        when(email.getKeywords()).thenReturn(null);
        when(emailBuilder.clearKeywords()).thenReturn(emailBuilder);
        when(emailBuilder.id(any())).thenReturn(emailBuilder);
        when(emailBuilder.threadId(any())).thenReturn(emailBuilder);
        when(emailBuilder.blobId(any())).thenReturn(emailBuilder);
        when(emailBuilder.receivedAt(any())).thenReturn(emailBuilder);
        when(emailBuilder.bodyStructure(any())).thenReturn(emailBuilder);
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        MethodResponse[] methodResponses = emailLogic.set(methodCall, previousResponses);
        Assertions.assertNotNull(methodResponses);
        Assertions.assertEquals(1, methodResponses.length);
        Assertions.assertTrue(methodResponses[0] instanceof SetEmailMethodResponse);
        Map<String, Map<String, Object>> updateMap = Collections.singletonMap("emailId", Collections.singletonMap("property", "value"));
        emailLogic.set(new SetEmailMethodCall("accountId", "testState", null, updateMap, null, null), previousResponses);
        Map<String, Email> create = new HashMap<>();
        create.put("emailId", email);
        emailLogic.set(new SetEmailMethodCall("accountId", "testState", create, updateMap, null, null), previousResponses);
        when(emailBuilder.build()).thenReturn(email);
        when(emailBuilder.mailboxId(any(), any())).thenReturn(emailBuilder);
        updateMap = Collections.singletonMap("emailId", Collections.singletonMap("keywords/test", "value"));
        emailLogic.set(new SetEmailMethodCall("accountId", "testState", null, updateMap, null, null), previousResponses);
        updateMap = Collections.singletonMap("emailId", Collections.singletonMap("keywords", "value"));
        emailLogic.set(new SetEmailMethodCall("accountId", "testState", null, updateMap, null, null), previousResponses);
        updateMap = Collections.singletonMap("emailId", Collections.singletonMap("mailboxIds/test", true));
        emailLogic.set(new SetEmailMethodCall("accountId", "testState", null, updateMap, null, null), previousResponses);
        Map<String, Boolean> map = new HashMap<>();
        map.put("emailId", true);
        updateMap = Collections.singletonMap("emailId", Collections.singletonMap("mailboxIds/test", map));
        emailLogic.set(new SetEmailMethodCall("accountId", "testState", null, updateMap, null, null), previousResponses);
        updateMap = Collections.singletonMap("emailId", Collections.singletonMap("mailboxIds", true));
        emailLogic.set(new SetEmailMethodCall("accountId", "testState", null, updateMap, null, null), previousResponses);
      }

    @Test
    public void changesTest() {
        ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
        String oldVersion = "testState";
        Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
        ChangesEmailMethodCall methodCall = new ChangesEmailMethodCall("accountId", "sinceState", 1024L);
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        Map<String, Update> updateMap = new HashMap<>();
        updateMap.put(oldVersion, oldUpdate);

        when(updatePort.getOf("accountId")).thenReturn(updateMap);
        when(oldUpdate.getChanges()).thenReturn(changesMap);
        when(oldUpdate.getChanges()).thenReturn(changesMap);
        when(statePort.get(any())).thenReturn("sinceState");

        MethodResponse[] methodResponses = emailLogic.changes(methodCall, previousResponses);
        Assertions.assertNotNull(methodResponses);
        Assertions.assertEquals(1, methodResponses.length);
        Assertions.assertTrue(methodResponses[0] instanceof ChangesEmailMethodResponse);
    }

    @Test
    public void changesTest1() {
        ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
        String oldVersion = "testState";
        Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
        ChangesEmailMethodCall methodCall = new ChangesEmailMethodCall("accountId", "testState", 1024L);
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        Map<String, Update> updateMap = new HashMap<>();
        updateMap.put(oldVersion, oldUpdate);

        when(updatePort.getOf("accountId")).thenReturn(updateMap);
        when(oldUpdate.getChanges()).thenReturn(changesMap);
        when(oldUpdate.getChanges()).thenReturn(changesMap);
        when(statePort.get(any())).thenReturn("sinceState");
        
        MethodResponse[] methodResponses = emailLogic.changes(methodCall, previousResponses);
        Assertions.assertNotNull(methodResponses);
        Assertions.assertEquals(1, methodResponses.length);
        Assertions.assertTrue(methodResponses[0] instanceof ChangesEmailMethodResponse);
    }

    @Test
    public void changesTestFailed() {
        ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
        String oldVersion = "testState";
        Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
        ChangesEmailMethodCall methodCall = new ChangesEmailMethodCall("accountId", "oldState", 1024L);
        EmailLogic emailLogic = new EmailLogic(emailPort, mailboxPort, statePort, updatePort);
        Map<String, Update> updateMap = new HashMap<>();
        updateMap.put(oldVersion, oldUpdate);

        when(updatePort.getOf("accountId")).thenReturn(updateMap);
        when(oldUpdate.getChanges()).thenReturn(changesMap);
        when(oldUpdate.getChanges()).thenReturn(changesMap);
        when(statePort.get(any())).thenReturn("sinceState");

        MethodResponse[] methodResponses = emailLogic.changes(methodCall, previousResponses);
        Assertions.assertNotNull(methodResponses);
        Assertions.assertEquals(1, methodResponses.length);
        Assertions.assertFalse(methodResponses[0] instanceof ChangesEmailMethodResponse);
    }

    @Test
    public void testInjectId() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getCharset()).thenReturn("UTF-8");
        when(attachment.getType()).thenReturn("application/pdf");
        when(attachment.getName()).thenReturn("test.pdf");
        when(attachment.getSize()).thenReturn(1024L);

        EmailBodyPart result = EmailLogic.injectId(attachment);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBlobId());
        Assertions.assertEquals("UTF-8", result.getCharset());
        Assertions.assertEquals("application/pdf", result.getType());
        Assertions.assertEquals("test.pdf", result.getName());
        Assertions.assertEquals(1024L, result.getSize());
    }
}

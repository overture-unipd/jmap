package it.unipd.overture.service;

import it.unipd.overture.Update;
import it.unipd.overture.port.out.EmailPort;
import it.unipd.overture.port.out.MailboxPort;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.UpdatePort;
import it.unipd.overture.service.MailboxInfo;
import it.unipd.overture.service.MailboxLogic;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.EmailBodyPart;
import rs.ltt.jmap.common.entity.Mailbox;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.mailbox.GetMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.SetMailboxMethodCall;
import rs.ltt.jmap.common.method.call.mailbox.ChangesMailboxMethodCall;
import rs.ltt.jmap.common.method.response.mailbox.GetMailboxMethodResponse;
import rs.ltt.jmap.common.method.response.mailbox.SetMailboxMethodResponse;
import rs.ltt.jmap.mock.server.Changes;
import rs.ltt.jmap.common.method.response.mailbox.ChangesMailboxMethodResponse;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MailboxLogicTest {
  @Mock EmailPort emailPort;
  @Mock MailboxPort mailboxPort;
  @Mock UpdatePort updatePort;
  @Mock StatePort statePort;
  @Mock Email email;
  @Mock Mailbox mailbox;
  @Mock Email.EmailBuilder emailBuilder;
  @Mock EmailBodyPart emailBodyPart;
  @Mock Map<String, MailboxInfo> mailboxInfoMap;
  @Mock MailboxInfo mailboxInfo;
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
      Map<String, Update> updateMap = new HashMap<>();
      updateMap.put(oldVersion, oldUpdate);

      when(updatePort.getOf(accountId)).thenReturn(updateMap);
      when(oldUpdate.getChanges()).thenReturn(changesMap);

      MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
      Update result = mailboxLogic.getAccumulatedUpdateSince(oldVersion, accountId);
      Assertions.assertNotNull(result);
    }

  @Test
  public void changesTest() {
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    String oldVersion = "testState";
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);
    when(updatePort.getOf("accountId")).thenReturn(updateMap);
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(statePort.get(any())).thenReturn("sinceState");
    MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
    MethodResponse[] methodResponses = mailboxLogic.changes(new ChangesMailboxMethodCall("accountId", "sinceState", 1024L), previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(1, methodResponses.length);
    Assertions.assertTrue(methodResponses[0] instanceof ChangesMailboxMethodResponse);
  }

  @Test
  public void changesTest2() {
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    String oldVersion = "testState";
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);
    when(updatePort.getOf("accountId")).thenReturn(updateMap);
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(statePort.get(any())).thenReturn("sinceState");
    MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
    MethodResponse[] methodResponses = mailboxLogic.changes(new ChangesMailboxMethodCall("accountId", "testState", 1024L), previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(1, methodResponses.length);
    Assertions.assertTrue(methodResponses[0] instanceof ChangesMailboxMethodResponse);
  }

  @Test
  public void changesTestFailed() {
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    String oldVersion = "testState";
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);
    when(updatePort.getOf("accountId")).thenReturn(updateMap);
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(statePort.get(any())).thenReturn("sinceState");
    MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
    MethodResponse[] methodResponses = mailboxLogic.changes(new ChangesMailboxMethodCall("accountId", "oldState", 1024L), previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(1, methodResponses.length);
    Assertions.assertFalse(methodResponses[0] instanceof ChangesMailboxMethodResponse);
  }

  @Test
  public void getTest() {
    String accountId = "accountId";
    MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
    ListMultimap<String, Response.Invocation> previousResponses = ArrayListMultimap.create();
    MethodResponse[] actualResponse = mailboxLogic.get(new GetMailboxMethodCall(accountId, new String[]{""}, new String[]{""}, null, null), previousResponses);
    String actualAccountId = ((GetMailboxMethodResponse) actualResponse[0]).getAccountId();
    Assertions.assertTrue(actualResponse[0] instanceof GetMailboxMethodResponse);
    Assertions.assertEquals(accountId, actualAccountId);
  }

  @Test
  public void getTest2() {
    String accountId = "accountId";
    MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
    ListMultimap<String, Response.Invocation> previousResponses = ArrayListMultimap.create();
    MethodResponse[] actualResponse = mailboxLogic.get(new GetMailboxMethodCall(accountId, new String[]{""}, Email.Properties.THREAD_ID, null, null), previousResponses);
    String actualAccountId = ((GetMailboxMethodResponse) actualResponse[0]).getAccountId();
    Assertions.assertTrue(actualResponse[0] instanceof GetMailboxMethodResponse);
    Assertions.assertEquals(accountId, actualAccountId);
  }

  @Test
  public void getTest3() {
    String accountId = "accountId";
    MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
    ListMultimap<String, Response.Invocation> previousResponses = ArrayListMultimap.create();
    MethodResponse[] actualResponse = mailboxLogic.get(new GetMailboxMethodCall(accountId, new String[]{""}, Email.Properties.MUTABLE, null, null), previousResponses);
    String actualAccountId = ((GetMailboxMethodResponse) actualResponse[0]).getAccountId();
    Assertions.assertTrue(actualResponse[0] instanceof GetMailboxMethodResponse);
    Assertions.assertEquals(accountId, actualAccountId);
  }

  @Test
  public void setTest() {
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    SetMailboxMethodCall methodCall = new SetMailboxMethodCall("accountId", "testState", null, null, new String[]{""}, null, false);
    when(statePort.get(any())).thenReturn("testState");
    when(emailPort.get(Mockito.any())).thenReturn(email);
    when(email.toBuilder()).thenReturn(emailBuilder);
    List<EmailBodyPart> emailBodyParts = new LinkedList<>();
    emailBodyParts.add(emailBodyPart);
    when(email.getTextBody()).thenReturn(emailBodyParts);
    when(emailBuilder.clearKeywords()).thenReturn(emailBuilder);
    when(emailBuilder.id(any())).thenReturn(emailBuilder);
    when(emailBuilder.threadId(any())).thenReturn(emailBuilder);
    when(emailBuilder.blobId(any())).thenReturn(emailBuilder);
    when(emailBuilder.receivedAt(any())).thenReturn(emailBuilder);
    when(emailBuilder.bodyStructure(any())).thenReturn(emailBuilder);
    MailboxLogic mailboxLogic = new MailboxLogic(mailboxPort, emailPort, statePort, updatePort);
    MethodResponse[] methodResponses = mailboxLogic.set(methodCall, previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(1, methodResponses.length);
    Assertions.assertTrue(methodResponses[0] instanceof SetMailboxMethodResponse);
    Map<String, Map<String, Object>> updateMap = Collections.singletonMap("emailId", Collections.singletonMap("property", "value"));
    mailboxLogic.set(new SetMailboxMethodCall("accountId", "testState", null, updateMap, new String[]{""}, null, false), previousResponses);
    Map<String, Mailbox> create = new HashMap<>();
    create.put("mailboxId", mailbox);
    mailboxLogic.set(new SetMailboxMethodCall("accountId", "testState", create, updateMap, new String[]{""}, null, false), previousResponses);
    when(mailboxPort.getOf(eq("accountId"))).thenReturn(mailboxInfoMap);
    when(mailboxInfoMap.get(any())).thenReturn(mailboxInfo);
    when(mailboxInfo.getId()).thenReturn("id");
    when(mailboxInfo.getName()).thenReturn("name");
    updateMap = Collections.singletonMap("emailId", Collections.singletonMap("role", "all"));
    mailboxLogic.set(new SetMailboxMethodCall("accountId", "testState", null, updateMap, new String[]{""}, null, false), previousResponses);
    updateMap = Collections.singletonMap("emailId", new HashMap<>());
    mailboxLogic.set(new SetMailboxMethodCall("accountId", "testState", null, updateMap, new String[]{""}, null, false), previousResponses);
    updateMap = new HashMap<>();
    mailboxLogic.set(new SetMailboxMethodCall("accountId", "testState", null, updateMap, new String[]{""}, null, false), previousResponses);
    mailboxLogic.set(new SetMailboxMethodCall("accountId", "kysState", create, updateMap, new String[]{""}, null, false), previousResponses);
  }
}

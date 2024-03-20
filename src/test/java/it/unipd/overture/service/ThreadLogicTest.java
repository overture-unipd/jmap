package it.unipd.overture.service;

import it.unipd.overture.service.ThreadLogic;
import it.unipd.overture.Update;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.ThreadPort;
import it.unipd.overture.port.out.UpdatePort;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.common.method.MethodResponse;
//  import rs.ltt.jmap.mock.server.Update;
import rs.ltt.jmap.common.method.call.thread.ChangesThreadMethodCall;
import rs.ltt.jmap.common.method.call.thread.GetThreadMethodCall;
import rs.ltt.jmap.common.method.response.thread.ChangesThreadMethodResponse;
import rs.ltt.jmap.common.method.response.thread.GetThreadMethodResponse;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ListMultimap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import rs.ltt.jmap.mock.server.Changes;


public class ThreadLogicTest {
  @Mock ThreadPort threadPort;
  @Mock UpdatePort updatePort;
  @Mock StatePort statePort;
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
    ThreadLogic threadLogic = new ThreadLogic(threadPort, updatePort, statePort);
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);

    when(updatePort.getOf(accountId)).thenReturn(updateMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);

    Update result = threadLogic.getAccumulatedUpdateSince(oldVersion, accountId);
    Assertions.assertNotNull(result);
  }

  @Test
  public void getAccumulatedUpdateSinceTestFailed() {
    String accountId = "accountId";
    String oldVersion = "oldVersion";
    String newVersion = "newVersion";
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    ThreadLogic threadLogic = new ThreadLogic(threadPort, updatePort, statePort);
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);

    when(updatePort.getOf(accountId)).thenReturn(updateMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);

    Update result = threadLogic.getAccumulatedUpdateSince(newVersion, accountId);
    Assertions.assertNull(result);
  }

  @Test
  public void changesTest() {
    String accountId = "accountId";
    String since = "sinceState";
    String oldVersion = "oldVersion";
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    ThreadLogic threadLogic = new ThreadLogic(threadPort, updatePort, statePort);
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);

    when(updatePort.getOf(accountId)).thenReturn(updateMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(statePort.get(any())).thenReturn(since);

    MethodResponse[] methodResponses = threadLogic.changes(new ChangesThreadMethodCall(accountId, since, 1024L), previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(1, methodResponses.length);
    Assertions.assertTrue(methodResponses[0] instanceof ChangesThreadMethodResponse);
  }

  @Test
  public void changesTest2() {
    String accountId = "accountId";
    String since = "sinceState";
    String oldVersion = "oldVersion";
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    ThreadLogic threadLogic = new ThreadLogic(threadPort, updatePort, statePort);
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);

    when(updatePort.getOf(accountId)).thenReturn(updateMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(statePort.get(any())).thenReturn(since);

    MethodResponse[] methodResponses = threadLogic.changes(new ChangesThreadMethodCall(accountId, oldVersion, 1024L), previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(1, methodResponses.length);
    Assertions.assertTrue(methodResponses[0] instanceof ChangesThreadMethodResponse);
  }

  @Test
  public void changesTestFailed() {
    String accountId = "accountId";
    String since = "sinceState";
    String oldVersion = "oldVersion";
    String wrongSinceState = "oldState";
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    ThreadLogic threadLogic = new ThreadLogic(threadPort, updatePort, statePort);
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    Map<String, Update> updateMap = new HashMap<>();
    updateMap.put(oldVersion, oldUpdate);

    when(updatePort.getOf(accountId)).thenReturn(updateMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(oldUpdate.getChanges()).thenReturn(changesMap);
    when(statePort.get(any())).thenReturn(since);

    MethodResponse[] methodResponses = threadLogic.changes(new ChangesThreadMethodCall(accountId, wrongSinceState, 1024L), previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(1, methodResponses.length);
    Assertions.assertFalse(methodResponses[0] instanceof ChangesThreadMethodResponse);
  }

  @Test
  public void getTest() {
    String accountId = "accountId";
    ListMultimap<String, Response.Invocation> previousResponses = mock(ListMultimap.class);
    ThreadLogic threadLogic = new ThreadLogic(threadPort, updatePort, statePort);
    MethodResponse[] methodResponses = threadLogic.get(new GetThreadMethodCall(accountId, new String[]{""}, null, null), previousResponses);
    Assertions.assertTrue(methodResponses[0] instanceof GetThreadMethodResponse);
  }
}

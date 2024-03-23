package it.unipd.overture.service;

import it.unipd.overture.Update;
import it.unipd.overture.port.out.AccountPort;
import it.unipd.overture.port.out.EmailPort;
import it.unipd.overture.port.out.EmailSubmissionPort;
import it.unipd.overture.port.out.MailboxPort;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.UpdatePort;
import it.unipd.overture.service.AuthenticationLogic;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.Response.Invocation;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.EmailBodyPart;
import rs.ltt.jmap.common.entity.EmailSubmission;
import rs.ltt.jmap.common.entity.Email.EmailBuilder;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.submission.SetEmailSubmissionMethodCall;
import rs.ltt.jmap.common.method.response.email.QueryEmailMethodResponse;
import rs.ltt.jmap.common.method.response.submission.SetEmailSubmissionMethodResponse;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmailSubmissionLogicTest {
  @Mock EmailPort emailPort;
  @Mock EmailSubmissionPort emailSubmissionPort;
  @Mock StatePort statePort;
  @Mock UpdatePort updatePort;
  @Mock Gson gson;
  @Mock SetEmailSubmissionMethodCall methodCall;
  @Mock EmailSubmission emailSubmission;
  @Mock Email email;
  @Mock EmailBuilder emailBuilder;
  @Mock Map<String, Object> updatesRef;
  @Mock List<String> keywords;
  @Mock Stream<String> stream;
  @Mock Stream streamI;
  @Mock Optional<String> optional;
  @Mock Set set;
  @Mock Collection<Invocation> collection;
  @Mock ListMultimap<String, Response.Invocation> previousResponses;
  @Mock Map<String, Map<String, Object>> map;


  @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

  @Test
  public void setTest() {
    String accountId = "accountId";
    String emailId = "emailId";
    String inState = "inState";
    Map<String, EmailSubmission> create = new HashMap<>();
    create.put("emailId", emailSubmission);
    EmailSubmissionLogic emailSubmissionLogic = new EmailSubmissionLogic(emailSubmissionPort, emailPort, statePort, updatePort, gson);

    when(methodCall.getAccountId()).thenReturn(accountId);
    when(statePort.get(accountId)).thenReturn(inState);
    when(methodCall.getCreate()).thenReturn(create);
    when(emailSubmission.getEmailId()).thenReturn(emailId);
    when(previousResponses.values()).thenReturn(collection);
    when(collection.stream()).thenReturn(streamI);
    when(streamI.filter(any())).thenReturn(streamI);
    when(streamI.map(any())).thenReturn(stream);
    when(stream.findFirst()).thenReturn(optional);
    when(optional.get()).thenReturn(emailId);
    when(emailPort.get(emailId)).thenReturn(email);
    when(email.toBuilder()).thenReturn(emailBuilder);
    when(methodCall.getOnSuccessUpdateEmail()).thenReturn(map);
    when(map.get("#"+"emailId")).thenReturn(updatesRef);
    when(updatesRef.keySet()).thenReturn(set);
    when(set.stream()).thenReturn(stream);
    when(stream.collect(Collectors.toList())).thenReturn(keywords);
    when(emailBuilder.build()).thenReturn(email);
    when(email.getId()).thenReturn(emailId);

    MethodResponse[] methodResponses = emailSubmissionLogic.set(methodCall, previousResponses);
    Assertions.assertNotNull(methodResponses);
    Assertions.assertEquals(2, methodResponses.length);
    Assertions.assertTrue(methodResponses[0] instanceof SetEmailSubmissionMethodResponse);
  }
}

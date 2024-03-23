package it.unipd.overture.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.inject.Inject;

import it.unipd.overture.Update;
import it.unipd.overture.port.out.EmailPort;
import it.unipd.overture.port.out.EmailSubmissionPort;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.UpdatePort;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.EmailSubmission;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.submission.SetEmailSubmissionMethodCall;
import rs.ltt.jmap.common.method.response.email.SetEmailMethodResponse;
import rs.ltt.jmap.common.method.response.submission.SetEmailSubmissionMethodResponse;

public class EmailSubmissionLogic {
  private EmailSubmissionPort emailSubmissionPort;  
  private EmailPort emailPort;
  private StatePort statePort;
  private UpdatePort updatePort;
  private Gson gson;

  @Inject
  EmailSubmissionLogic(EmailSubmissionPort emailSubmissionPort, EmailPort emailPort, StatePort statePort, UpdatePort updatePort, Gson gson) {
    this.emailSubmissionPort = emailSubmissionPort;
    this.emailPort = emailPort;
    this.statePort = statePort;
    this.updatePort = updatePort;
    this.gson = gson;
  }

  public MethodResponse[] set(SetEmailSubmissionMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    var accountid = methodCall.getAccountId();
    var oldState = statePort.get(accountid);
    Map<String, EmailSubmission> created = new HashMap<>();
    Map<String, Email> updated = new HashMap<>();
    methodCall.getCreate().forEach((k, v) -> {
      var emailRef = v.getEmailId().substring(1);
      String emailId = previousResponses.values().stream().filter(i -> i.getMethodResponse() instanceof SetEmailMethodResponse).filter(i -> {
        return ((SetEmailMethodResponse) i.getMethodResponse()).getCreated().containsKey(emailRef);
      }).map(i -> ((SetEmailMethodResponse) i.getMethodResponse()).getCreated().get(emailRef).getId()).findFirst().get();
      var oldEmail = emailPort.get(emailId);
      var newEmailBuilder = oldEmail.toBuilder();
      var updatesRef = methodCall.getOnSuccessUpdateEmail().get("#"+k);
      List<String> keywords = updatesRef.keySet().stream().filter(i -> i.startsWith("keywords/")).map(i -> i.substring("keywords/".length())).collect(Collectors.toList());
      keywords.stream().forEach(zp -> newEmailBuilder.keyword(zp, (Boolean) updatesRef.get(zp)));
      Map<String,Boolean> prop = gson.fromJson(gson.toJson(updatesRef.get("mailboxIds")), Map.class); 
      newEmailBuilder.mailboxIds(prop);
      var newEmail = newEmailBuilder.build();
      emailPort.insert(accountid, newEmail);
      String submissionId = UUID.randomUUID().toString();
      var submission = EmailSubmission.builder().emailId(emailId).identityId(k).build();
      emailSubmissionPort.insert(accountid, submissionId, submission);
      created.put(k, gson.fromJson("{\"id\":\""+submissionId+"\"}", EmailSubmission.class));
      updated.put(emailId, Email.builder().build());
      var oldS = statePort.get(accountid);
      statePort.increment(accountid);
      var news = statePort.get(accountid);
      updatePort.insert(accountid, oldS, Update.updated(new ArrayList<Email>(Arrays.asList(newEmail)), new HashSet<>(Arrays.asList()), news));
    });
    var newState = statePort.get(accountid);
    return new MethodResponse[] {
      SetEmailSubmissionMethodResponse.builder()
        .accountId(accountid)
        .oldState(oldState)
        .newState(newState)
        .created(created)
        .build(),
      SetEmailMethodResponse.builder()
        .accountId(accountid)
        .oldState(oldState)
        .newState(newState)
        .updated(updated)
        .build()
    };
  }
}

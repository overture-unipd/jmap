package it.unipd.overture.service;

import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;

import it.unipd.overture.port.out.EmailSubmissionPort;
import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.submission.GetEmailSubmissionMethodCall;
import rs.ltt.jmap.common.method.call.submission.SetEmailSubmissionMethodCall;

public class EmailSubmissionLogic {
  EmailSubmissionPort submission;  

  @Inject
  EmailSubmissionLogic(EmailSubmissionPort submission) {
    this.submission = submission;
  }

  public MethodResponse[] set(SetEmailSubmissionMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
  }

  public MethodResponse[] get(GetEmailSubmissionMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return null;
  }
}

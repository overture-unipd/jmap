package it.unipd.overture.port.out;

import rs.ltt.jmap.common.entity.EmailSubmission;

public interface EmailSubmissionPort {
  EmailSubmission get(String id);
  void insert(String accountid, String id, EmailSubmission submission);
}

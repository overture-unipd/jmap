package it.unipd.overture.port.out;

public interface EmailSubmissionPort {
  String get(String id);
  String insert(String submission);
}

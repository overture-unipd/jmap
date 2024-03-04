package it.unipd.overture.ports.out;

public interface EmailSubmissionPort {
  String get(String id);
  String insert(String submission);
}

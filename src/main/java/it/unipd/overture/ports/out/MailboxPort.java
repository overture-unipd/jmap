package it.unipd.overture.ports.out;

public interface MailboxPort {
  String get(String id);
  String getOf(String accountid);
  String insert(String mailbox);
  void delete(String id);
}

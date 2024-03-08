package it.unipd.overture.port.out;

public interface MailboxPort {
  String get(String id);
  String getOf(String accountid);
  String insert(String mailbox);
  void delete(String id);
}

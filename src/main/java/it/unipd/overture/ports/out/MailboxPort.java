package it.unipd.overture.ports.out;

public interface MailboxPort {
  String getMailbox(String id);
  String getAccountMailboxes(String id);
  String insertMailbox(String mailbox);
  void deleteMailbox(String id);
}

package it.unipd.overture.port.out;

import java.util.Map;

import it.unipd.overture.service.MailboxInfo;

public interface MailboxPort {
  MailboxInfo get(String id);
  Map<String, MailboxInfo> getOf(String accountid);
  void insert(String accountid, MailboxInfo mailbox);
  void delete(String id);
}

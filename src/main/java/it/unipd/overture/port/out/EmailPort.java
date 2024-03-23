package it.unipd.overture.port.out;

import java.util.Map;

import rs.ltt.jmap.common.entity.Email;

public interface EmailPort {
  Email get(String id);
  Map<String, Email> getOf(String accountid);
  void insert(String accountid, Email email);
  void delete(String id);
}

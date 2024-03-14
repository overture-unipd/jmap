package it.unipd.overture.port.out;

import java.util.Map;

import it.unipd.overture.Update;

public interface UpdatePort {
  Update get(String accountid, String state);
  Map<String, Update> getOf(String accountid);
  void insert(String accountid, String oldstate, Update update);
}

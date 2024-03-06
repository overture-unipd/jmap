package it.unipd.overture.ports.out;

public interface IdentityPort {
  String getAll(String accountid);
  String getFirst(String accountid);
  // String getName(String id);
  // String getAddress(String id);
}

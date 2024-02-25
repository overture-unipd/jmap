package it.unipd.overture.ports.out;

public interface IdentityPort {
  String getIdentities(String accountid);
  String getFirstIdentity(String accountid);
  // String getName(String id);
  // String getAddress(String id);
}

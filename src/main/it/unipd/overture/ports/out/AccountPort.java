package it.unipd.overture.ports.out;

public interface AccountPort {
  String getAccountAddress(String id);
  String getAccountPassword(String id);
  String getAccountName(String id);
  String insertAccount(String account); // account id
}

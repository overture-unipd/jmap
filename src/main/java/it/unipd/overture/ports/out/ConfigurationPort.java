package it.unipd.overture.ports.out;

public interface ConfigurationPort {
  String getDBHost();
  String getDBPort();
  String getDBDb();
  String getDbAccounts();
}

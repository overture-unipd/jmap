package it.unipd.overture.port.out;

public interface AccountPort {
  String getId(String username);
  String getPassword(String id);
  String getState(String id);
  void incrementState(String id);
}

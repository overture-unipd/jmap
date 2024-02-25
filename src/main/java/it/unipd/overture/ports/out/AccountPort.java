package it.unipd.overture.ports.out;

public interface AccountPort {
  String getId(String username);
  String getPassword(String id);
  String getState(String id);
  void incrementState(String id);
}

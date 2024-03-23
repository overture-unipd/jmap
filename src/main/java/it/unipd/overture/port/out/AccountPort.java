package it.unipd.overture.port.out;

public interface AccountPort {
  String getId(String username);
  String getPassword(String id);
}

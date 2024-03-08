package it.unipd.overture.port.out;

public interface EmailPort {
  String get(String id);
  String insert(String email);
  void delete(String id);
}

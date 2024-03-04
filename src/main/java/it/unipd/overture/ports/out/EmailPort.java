package it.unipd.overture.ports.out;

public interface EmailPort {
  String get(String id);
  String insert(String email);
  void delete(String id);
}

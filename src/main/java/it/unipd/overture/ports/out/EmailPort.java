package it.unipd.overture.ports.out;

public interface EmailPort {
  String getEmail(String id);
  String insertEmail(String email); // returns id
  void deleteEmail(String id);
}
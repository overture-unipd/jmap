package it.unipd.overture.ports.out;

public interface UpdatePort {
  String get(String id);
  String insert(String update);
  void delete(String id);
}

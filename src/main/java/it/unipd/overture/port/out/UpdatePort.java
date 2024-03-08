package it.unipd.overture.port.out;

public interface UpdatePort {
  String get(String id);
  String insert(String update);
  void delete(String id);
}

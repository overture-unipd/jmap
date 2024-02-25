package it.unipd.overture.ports.out;

public interface UpdatePort {
  String getUpdate(String id);
  String insertUpdate(String update);
  void deleteUpdate(String id);
}

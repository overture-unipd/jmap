package it.unipd.overture.ports.out;

public interface UpdatePort {
  String insertUpdate(String update); // returns id
  String getUpdate(String id);
  void deleteUpdate(String id);
}

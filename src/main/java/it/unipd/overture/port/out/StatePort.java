package it.unipd.overture.port.out;

public interface StatePort {
  String get(String accountid);
  void increment(String accountid);
}

package it.unipd.overture.ports.in;

public interface MethodPort {
  Boolean authenticate(String auth);
  String session(String auth);
  String jmap(String json);
  String upload(byte[] data);
  byte[] download(String id);
}

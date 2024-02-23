package it.unipd.overture.ports.in;

public interface RequestPort {
  String wellKnown();
  String session(String json);
  String postJmap(String id);
  byte[] download(String id);
  void upload(Byte[] data);
}

package it.unipd.overture.port.out;

public interface AttachmentPort {
  byte[] get(String id);
  String insert(byte[] data);
  boolean delete(String id);
}

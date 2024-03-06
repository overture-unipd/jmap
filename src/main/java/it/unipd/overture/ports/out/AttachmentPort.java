package it.unipd.overture.ports.out;

public interface AttachmentPort {
  byte[] get(String id);
  String insert(byte[] data);
  boolean delete(String id);
}

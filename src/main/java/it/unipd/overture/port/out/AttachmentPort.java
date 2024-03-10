package it.unipd.overture.port.out;

public interface AttachmentPort {
  byte[] get(String id);
  String insert(byte[] data, String contentType, Long size);
  Boolean delete(String id);
}

package it.unipd.overture.ports.out;

public interface AttachmentPort {
  byte[] getAttachment(String id);
  String insertAttachment(byte[] data);
  boolean deleteAttachment(String id);
}

package it.unipd.overture.ports.out;

public interface AttachmentPort {
  String getAttachment(String id);
  String insertAttachment(byte[] attachment);
  void deleteAttachment(String id);
}

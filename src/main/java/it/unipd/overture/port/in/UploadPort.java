package it.unipd.overture.port.in;

public interface UploadPort {
  String push(byte[] data, String type, Long size);
}

package it.unipd.overture.ports.in;

public interface UploadPort {
  String push(byte[] data);
}

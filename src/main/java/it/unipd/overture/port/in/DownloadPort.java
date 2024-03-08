package it.unipd.overture.port.in;

public interface DownloadPort {
  byte[] pull(String id);
}

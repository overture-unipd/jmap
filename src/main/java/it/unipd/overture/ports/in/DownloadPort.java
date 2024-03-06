package it.unipd.overture.ports.in;

public interface DownloadPort {
  byte[] pull(String id);
}

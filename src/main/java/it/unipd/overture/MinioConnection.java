package it.unipd.overture;

import com.google.inject.Provides;

import io.minio.MinioClient;

public class MinioConnection {
  private MinioClient conn;  

  public MinioConnection(String host, int port, String buckname, String access, String secret) {
    this.conn = MinioClient.builder()
      .endpoint(host, port, false)
      .credentials(access, secret)
      .build();    
  }

  @Provides
  public MinioClient provideConnection() {
    return conn;
  }
}

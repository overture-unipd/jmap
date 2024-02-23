package it.unipd.overture.adapters.out;

import io.minio.MinioClient;

public class MinioConnection {
  private MinioClient conn;  
  private BucketName buckname;

  public MinioConnection(String host, int port, String buckname, String access, String secret) {
    this.conn = MinioClient.builder()
      .endpoint(host, port, false)
      .credentials(access, secret)
      .build();    
    this.buckname = new BucketName(buckname);
  }

  public MinioClient provideConnection() {
    return conn;
  }

  public BucketName provideBucketName() {
    return buckname;
  }

  public class BucketName {
    String name;

    BucketName(String name) {
      this.name = name;
    }

    String getName() {
      return name;
    }
  }
}

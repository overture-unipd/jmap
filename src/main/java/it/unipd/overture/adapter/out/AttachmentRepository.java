package it.unipd.overture.adapter.out;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import it.unipd.overture.port.out.AttachmentPort;

public class AttachmentRepository implements AttachmentPort {
  private MinioClient conn;
  private String bucket;

  @Inject
  AttachmentRepository(MinioClient conn, @Named("MINIO_BUCKET") String bucket) {
    this.conn = conn;
    this.bucket = bucket;
  }

  @Override
  public byte[] get(String id) {
    try {
      return conn.getObject(
        GetObjectArgs.builder()
          .bucket(bucket)
          .object(id)
          .build()).readAllBytes();
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public Boolean delete(String id) {
    try {
      conn.removeObject(
        RemoveObjectArgs.builder()
          .bucket(bucket)
          .object(id)
          .build());
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  @Override
  public String insert(byte[] data, String contentType, Long size) {
    try {
      var name = UUID.randomUUID().toString();
      conn.putObject(
        PutObjectArgs.builder()
          .bucket(bucket)
          .object(name)
          .contentType(contentType)
          .stream(new ByteArrayInputStream(data), size, -1)
          .build());
        return name;
    } catch (Exception e) {
    }
    return null;
  }
}

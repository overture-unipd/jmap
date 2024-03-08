package it.unipd.overture.adapter.out;

import java.io.ByteArrayInputStream;

import com.google.inject.Inject;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import it.unipd.overture.port.out.AttachmentPort;

import java.util.UUID;

public class AttachmentRepository implements AttachmentPort {
  private MinioClient conn;

  @Inject
  AttachmentRepository(MinioClient conn) {
    this.conn = conn;
  }

  @Override
  public byte[] get(String id) {
    try {
      return conn.getObject(
        GetObjectArgs.builder()
          .bucket("jmap")
          .object(id)
          .build()).readAllBytes();
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public boolean delete(String id) {
    try {
      conn.removeObject(
        RemoveObjectArgs.builder()
          .bucket("jmap")
          .object(id)
          .build());
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  @Override
  public String insert(byte[] data) {
    try {
      var name = UUID.randomUUID().toString();
      conn.putObject(
        PutObjectArgs.builder()
          .bucket("jmap")
          .object(name)
          .stream(new ByteArrayInputStream(data), 0, -1)
          .build());
        return name;
    } catch (Exception e) {
    }
    return null;
  }
}

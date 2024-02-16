package it.unipd.overture.jmap;

import org.junit.jupiter.api.Test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Connection;

import com.google.gson.Gson;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Base64;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@Testcontainers
public class DispatcherTest {
  private final RethinkDB r = RethinkDB.r;
  private Dispatcher dispatcher;
  private String db_host;
  private int db_port;
  private String db_name;
  private Connection conn;
  @Container
  private final GenericContainer<?> rethinkDBContainer =
    new GenericContainer<>("rethinkdb:latest")
      .withExposedPorts(28015)
      .waitingFor(Wait.forLogMessage(".*Server ready.*\\n", 1));

  @BeforeEach
  void setUp() {
    rethinkDBContainer.start();
    this.db_host = rethinkDBContainer.getHost();
    this.db_port = rethinkDBContainer.getMappedPort(28015);
    this.db_name = "test";
    this.conn = r.connection().hostname(db_host).port(db_port).connect();
    r.tableCreate("attachment").run(conn);
    dispatcher = new Dispatcher(new Database(db_host, db_port, db_name));
    System.out.println(db_port);
  }

  @Test
  void download() {
    byte[] blob = { 1, 2, 3, 4 };
    Gson gson = new Gson();
    String blobId;

    var id = r.table("attachment").insert(r.hashMap("content", blob)).toJson().run(conn);
    blobId = gson.fromJson(id.first().toString(), JsonObject.class).get("generated_keys").getAsString();

    byte[] downloadedBlob = dispatcher.download(blobId);

    assertArrayEquals(blob, downloadedBlob);
  }

  @Test
  void upload() {
    String type = "image/jpeg";
    long size = 4;
    byte[] blob = {1, 2, 3, 4};
  
    String result = dispatcher.upload(type, size, blob);

    Gson gson = new Gson();
    var blobId = gson.fromJson(result, JsonObject.class).get("blobId").getAsString();

    var cursor = r.table("attachment").get(blobId).pluck("content").toJson().run(this.conn);
    byte[] uploadedblob =
      Base64.getDecoder().decode(gson.fromJson(cursor.first().toString(), JsonObject.class)
        .getAsJsonObject("content")
        .get("data")
        .getAsString().getBytes());
    assertArrayEquals(blob, uploadedblob);
  }
}

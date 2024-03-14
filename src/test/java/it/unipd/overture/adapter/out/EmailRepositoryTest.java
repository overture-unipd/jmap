package it.unipd.overture.adapter.out;

import org.junit.jupiter.api.Test;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.TestInstance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.port.out.EmailPort;
import rs.ltt.jmap.gson.JmapAdapters;
import rs.ltt.jmap.common.entity.Email;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailRepositoryTest {
  private final RethinkDB r = RethinkDB.r;
  private final String db_name = "jmap";
  private Connection conn;
  private final GenericContainer<?> rethinkDBContainer = new GenericContainer<>("rethinkdb:2.4.2-bullseye-slim")
      .withExposedPorts(28015)
      .waitingFor(Wait.forLogMessage(".*Server ready.*\\n", 1));
  private Gson gson;

  @BeforeAll
  void setUp() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    this.gson = gsonBuilder.create();
    rethinkDBContainer.start();
    this.conn = r.connection().hostname(rethinkDBContainer.getHost()).port(rethinkDBContainer.getMappedPort(28015))
        .connect();
    r.dbCreate(db_name).run(this.conn);
    conn.use(db_name);
    r.tableCreate("email").run(this.conn);
    r.table("email").indexCreate("account").run(conn);
    r.table("email").indexWait().run(conn);
  }

  @Test
  void testGet() {
    EmailPort repo = new EmailRepository(conn, gson);
    String emailJson = "{\"id\":\"justanid\"}";
    r.table("email").insert(r.json(emailJson)).run(conn);
    Email email = repo.get("justanid");
    Assertions.assertEquals(gson.toJson(email), emailJson);
    Assertions.assertEquals(null, repo.get("unexistent"));
  }

  @Test
  void testGetOf() {
    EmailPort repo = new EmailRepository(conn, gson);
    Email e1 = Email.builder().id("id1").blobId("test1").build();
    Email e2 = Email.builder().id("id2").blobId("test2").build();
    String account = "alice";

    r.table("email")
      .insert(r.json("{\"account\":\""+account+"\","+gson.toJson(e1).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);
    r.table("email")
      .insert(r.json("{\"account\":\""+account+"\","+gson.toJson(e2).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);

    Map<String, Email> map = repo.getOf("alice");
    Assertions.assertEquals(gson.toJson(map.get("id1"), Email.class), gson.toJson(e1, Email.class));
  }

  @Test
  void testInsert() {
    EmailPort repo = new EmailRepository(conn, gson);
    Email email = Email.builder().id("justanid").build();
    repo.insert("bob", email);
    String res = r.table("email")
                   .get("justanid")
                   .without("account")
                   .toJson()
                   .run(conn)
                   .single()
                   .toString();
    Assertions.assertEquals(gson.toJson(email), res);
  }

  @Test
  void testDelete() {
    String emailJson = "{\"id\":\"aliceid\",\"account\":\"aliceaccount\"}";
    r.table("email").insert(r.json(emailJson)).run(conn);
    EmailPort repo = new EmailRepository(conn, gson);
    repo.delete("aliceid");
    Assertions.assertEquals(r.table("email").get("aliceid").run(conn).first(), null);
  }
}

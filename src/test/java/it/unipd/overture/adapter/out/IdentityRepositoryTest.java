package it.unipd.overture.adapter.out;

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

import it.unipd.overture.port.out.IdentityPort;

import org.junit.jupiter.api.Test;
import rs.ltt.jmap.common.entity.Identity;
import rs.ltt.jmap.gson.JmapAdapters;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IdentityRepositoryTest {
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
    r.tableCreate("identity").run(this.conn);
    r.table("identity").indexCreate("account").run(conn);
    r.table("identity").indexWait().run(conn);
  }

  @Test
  void testGetOf() {
    Identity i1 = Identity.builder().name("alice1").id("aliceid2").build();
    Identity i2 = Identity.builder().name("alice2").id("aliceid1").build();

    String account = "alice";

    r.table("identity")
        .insert(r.json("{\"account\":\"" + account + "\"," + gson.toJson(i1).substring(1)))
        .optArg("conflict", "replace")
        .run(conn);
    r.table("identity")
        .insert(r.json("{\"account\":\"" + account + "\"," + gson.toJson(i2).substring(1)))
        .optArg("conflict", "replace")
        .run(conn);

    IdentityPort repo = new IdentityRepository(conn, gson);
    Identity[] identity = repo.getOf("alice");

    Assertions.assertEquals(gson.toJson(identity[0], Identity.class), gson.toJson(i1, Identity.class));
  }
}

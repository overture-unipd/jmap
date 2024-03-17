package it.unipd.overture.adapter.out;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
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

import it.unipd.overture.port.out.ThreadPort;
import rs.ltt.jmap.gson.JmapAdapters;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ThreadRepositoryTest {
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
  void testGetOf() {
    String e1 = "{\"id\":\"e1\",\"account\":\"aliceid\",\"threadId\":\"t1\"}";
    String e2 = "{\"id\":\"e2\",\"account\":\"aliceid\",\"threadId\":\"t2\"}";
    String e3 = "{\"id\":\"e3\",\"account\":\"aliceid\",\"threadId\":\"t1\"}";

    r.table("email").insert(r.json(e1)).run(conn);
    r.table("email").insert(r.json(e2)).run(conn);
    r.table("email").insert(r.json(e3)).run(conn);

    ThreadPort repo = new ThreadRepository(conn);
    var res = repo.getOf("aliceid", "t1");
    repo.getOf("alicid", "t1");

    Assertions.assertEquals(gson.toJson(res), gson.toJson(Arrays.asList("e1", "e3")));
  }
}

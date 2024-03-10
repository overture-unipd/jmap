package it.unipd.overture.adapter.out;

import org.junit.jupiter.api.Test;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.TestInstance;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.port.out.StatePort;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StateRepositoryTest {
  private final RethinkDB r = RethinkDB.r;
  private final String db_name = "jmap";
  private Connection conn;
  private final GenericContainer<?> rethinkDBContainer = new GenericContainer<>("rethinkdb:2.4.2-bullseye-slim")
      .withExposedPorts(28015)
      .waitingFor(Wait.forLogMessage(".*Server ready.*\\n", 1));

  @BeforeAll
  void setUp() {
    rethinkDBContainer.start();
    this.conn = r.connection().hostname(rethinkDBContainer.getHost()).port(rethinkDBContainer.getMappedPort(28015))
        .connect();
    r.dbCreate(db_name).run(this.conn);
    conn.use(db_name);
    r.tableCreate("account").run(this.conn);
    r.table("account").indexWait().run(conn);
  }

  @Test
  void testGetState() {
    String accountJson = "{\"id\":\"aliceid\",\"username\":\"alice\",\"password\":\"pw\",\"state\":\"42\"}";
    r.table("account").insert(r.json(accountJson)).optArg("conflict", "replace").run(conn);
    StatePort repo = new StateRepository(conn);
    repo.get("alicei");
    Assertions.assertEquals("42", repo.get("aliceid"));
  }

  @Test
  void testIncrementState() {
    String accountJson = "{\"id\":\"aliceid\",\"username\":\"alice\",\"password\":\"pw\",\"state\":\"0\"}";
    r.table("account").insert(r.json(accountJson)).optArg("conflict", "replace").run(conn);
    StatePort repo = new StateRepository(conn);
    repo.increment("aliceid");
    String state = r.table("account").get("aliceid").run(conn, Properties.class).single().getProperty("state");
    Assertions.assertEquals("1", state);
  }
}

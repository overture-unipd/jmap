package it.unipd.overture.adapter.out;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import it.unipd.overture.port.out.AccountPort;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountRepositoryTest {
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
    r.table("account").indexCreate("username").run(conn);
    r.table("account").indexWait().run(conn);
  }

  @Test
  void testGetId() {
    AccountPort accountPort = new AccountRepository(conn);
    String accountJson = "{\"id\":\"aliceid\",\"username\":\"alice\",\"password\":\"ecila\"}";
    r.table("account").insert(r.json(accountJson)).run(conn);
    Assertions.assertEquals("aliceid", accountPort.getId("alice"));
    Assertions.assertEquals(null, accountPort.getId("alic"));
  }

  @Test
  void testGetPassword() {
    AccountPort accountPort = new AccountRepository(conn);
    String accountJson = "{\"id\":\"aliceid\",\"username\":\"alice\",\"password\":\"ecila\"}";
    r.table("account").insert(r.json(accountJson)).run(conn);
    Assertions.assertEquals("ecila", accountPort.getPassword("aliceid"));
    Assertions.assertEquals(null, accountPort.getPassword("alicei"));
  }
}

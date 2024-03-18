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

import it.unipd.overture.port.out.MailboxPort;
import it.unipd.overture.service.MailboxInfo;
import rs.ltt.jmap.common.entity.Role;
import rs.ltt.jmap.gson.JmapAdapters;
// import rs.ltt.jmap.common.entity.Mailbox;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MailboxRepositoryTest {
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
    r.tableCreate("mailbox").run(this.conn);
    r.table("mailbox").indexCreate("account").run(conn);
    r.table("mailbox").indexWait().run(conn);
  }

  @Test
  void testGet() {
    MailboxPort repo = new MailboxRepository(conn, gson);
    MailboxInfo m1 = new MailboxInfo("id1", "inbox", Role.INBOX);
    r.table("mailbox").insert(r.json(gson.toJson(m1))).run(conn);
    MailboxInfo mailbox = repo.get("id1");
    Assertions.assertEquals(gson.toJson(mailbox), gson.toJson(m1));
  }

  @Test
  void testGetOf() {
    MailboxPort repo = new MailboxRepository(conn, gson);
    MailboxInfo m1 = new MailboxInfo("id1", "inbox", Role.INBOX);
    MailboxInfo m2 = new MailboxInfo("id2", "inbox", Role.INBOX);
    String account = "alice";

    r.table("mailbox")
      .insert(r.json("{\"account\":\""+account+"\","+gson.toJson(m1).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);
    r.table("mailbox")
      .insert(r.json("{\"account\":\""+account+"\","+gson.toJson(m2).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);

    Map<String, MailboxInfo> map = repo.getOf("alice");
    Assertions.assertEquals(gson.toJson(map.get("id1"), MailboxInfo.class), gson.toJson(m1, MailboxInfo.class));
  }

  @Test
  void testInsert() {
    MailboxPort repo = new MailboxRepository(conn, gson);
    MailboxInfo m1 = new MailboxInfo("id1", "inbox", Role.INBOX);
    repo.insert("bob", m1);
    String res = r.table("mailbox")
                   .get("id1")
                   .without("account")
                   .toJson()
                   .run(conn)
                   .single()
                   .toString();
    Assertions.assertEquals(gson.toJson(m1), res);
  }

  @Test
  void testDelete() {
    MailboxInfo m1 = new MailboxInfo("id1", "inbox", Role.INBOX);
    r.table("mailbox").insert(r.json(gson.toJson(m1, MailboxInfo.class))).run(conn);
    MailboxPort repo = new MailboxRepository(conn, gson);
    repo.delete("aliceid");
    Assertions.assertEquals(r.table("mailbox").get("aliceid").run(conn).first(), null);
  }
}

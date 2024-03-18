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
import it.unipd.overture.port.out.EmailSubmissionPort;
import rs.ltt.jmap.gson.JmapAdapters;
import rs.ltt.jmap.common.entity.EmailSubmission;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailSubmissionRepositoryTest {
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
    r.tableCreate("emailsubmission").run(this.conn);
    r.table("emailsubmission").indexCreate("account").run(conn);
    r.table("emailsubmission").indexWait().run(conn);
  }

  @Test
  void testGet() {
    EmailSubmissionPort repo = new EmailSubmissionRepository(conn, gson);
    String emailJson = "{\"identityId\":\"idid\",\"id\":\"aabbcc\"}";
    r.table("emailsubmission").insert(r.json(emailJson)).run(conn);
    EmailSubmission email = repo.get("aabbcc");
    Assertions.assertEquals(gson.toJson(email, EmailSubmission.class), gson.toJson(gson.fromJson(emailJson, EmailSubmission.class)));
    Assertions.assertEquals(null, repo.get("abracadra"));
  }

  @Test
  void testInsert() {
    EmailSubmissionPort repo = new EmailSubmissionRepository(conn, gson);
    EmailSubmission email = EmailSubmission.builder().identityId("idid").build();
    repo.insert("bob", "idid", email);
    Map res = r.table("emailsubmission")
                   .get("idid")
                   .without("id")
                   .without("account")
                   .run(conn, Map.class).single();
    Assertions.assertEquals(gson.toJson(email, EmailSubmission.class), gson.toJson(gson.fromJson(gson.toJson(res), EmailSubmission.class)));
  }
}

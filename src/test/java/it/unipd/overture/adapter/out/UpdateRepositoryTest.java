package it.unipd.overture.adapter.out;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.Update;
import it.unipd.overture.UpdateDeserializer;
import it.unipd.overture.UpdateSerializer;
import it.unipd.overture.port.out.UpdatePort;
import rs.ltt.jmap.gson.JmapAdapters;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdateRepositoryTest {
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
    gsonBuilder.registerTypeAdapter(Update.class, new UpdateSerializer());
    gsonBuilder.registerTypeAdapter(Update.class, new UpdateDeserializer());
    this.gson = gsonBuilder.create();
    rethinkDBContainer.start();
    this.conn = r.connection().hostname(rethinkDBContainer.getHost()).port(rethinkDBContainer.getMappedPort(28015))
        .connect();
    r.dbCreate(db_name).run(this.conn);
    conn.use(db_name);
    r.tableCreate("update").run(this.conn);
    r.table("update").indexCreate("account").run(conn);
    r.table("update").indexWait().run(conn);
  }

  @Test
  void testGet() {
    UpdatePort repo = new UpdateRepository(conn, gson);
    String updateJson = "{\"account\":\"1d894a70-35ef-43f7-9dbb-31e109b24564\",\"changes\":{\"rs.ltt.jmap.common.entity.Email\":{\"created\":[],\"updated\":[\"3709b25f-f1d4-421d-ae07-dc2d2e8915f3\",\"47988266-7eaf-49f7-8c49-6f73b4c86d81\"]},\"rs.ltt.jmap.common.entity.Mailbox\":{\"created\":[],\"updated\":[\"2fbbe51a-ac62-4d2f-a258-8e6792bde83a\"]},\"rs.ltt.jmap.common.entity.Thread\":{\"created\":[],\"updated\":[]}},\"id\":\"53f53a57-5e5a-491b-9ad9-cc02367731bd\",\"newVersion\":\"1\",\"state\":\"0\"}";
    r.table("update").insert(r.json(updateJson)).run(conn);
    Update update = repo.get("1d894a70-35ef-43f7-9dbb-31e109b24564", "0");
    Assertions.assertEquals(gson.toJson(update), gson.toJson(gson.fromJson(updateJson, Update.class)));
    Assertions.assertEquals(null, repo.get("ziopera", "0"));
  }

  @Test
  void testGetOf() {
    UpdatePort repo = new UpdateRepository(conn, gson);

    String u1 = "{\"account\":\"1d894a70-35ef-43f7-9dbb-31e109b24564\",\"changes\":{\"rs.ltt.jmap.common.entity.Email\":{\"created\":[],\"updated\":[\"3709b25f-f1d4-421d-ae07-dc2d2e8915f3\",\"47988266-7eaf-49f7-8c49-6f73b4c86d81\"]},\"rs.ltt.jmap.common.entity.Mailbox\":{\"created\":[],\"updated\":[\"2fbbe51a-ac62-4d2f-a258-8e6792bde83a\"]},\"rs.ltt.jmap.common.entity.Thread\":{\"created\":[],\"updated\":[]}},\"id\":\"53f53a57-5e5a-491b-9ad9-cc02367731bd\",\"newVersion\":\"1\",\"state\":\"0\"}";
    String u2 = "{\"account\":\"1d894a70-35ef-43f7-9dbb-31e109b24564\",\"changes\":{\"rs.ltt.jmap.common.entity.Email\":{\"created\":[],\"updated\":[\"3709b25f-f1d4-421d-ae07-dc2d2e8915f3\",\"47988266-7eaf-49f7-8c49-6f73b4c86d81\"]},\"rs.ltt.jmap.common.entity.Mailbox\":{\"created\":[],\"updated\":[\"2fbbe51a-ac62-4d2f-a258-8e6792bde83a\"]},\"rs.ltt.jmap.common.entity.Thread\":{\"created\":[],\"updated\":[]}},\"id\":\"43f53a57-5e5a-491b-9ad9-cc02367731bd\",\"newVersion\":\"2\",\"state\":\"1\"}";

    r.table("update").insert(r.json(u1)).run(conn);
    r.table("update").insert(r.json(u2)).run(conn);

    Map<String,Update> updates = repo.getOf("1d894a70-35ef-43f7-9dbb-31e109b24564");

    Assertions.assertEquals(gson.toJson(gson.fromJson(u1, Update.class)), gson.toJson(updates.get("0"), Update.class));
    Assertions.assertEquals(gson.toJson(gson.fromJson(u2, Update.class)), gson.toJson(updates.get("1"), Update.class));
  }

  @Test
  void testInsert() {
    UpdatePort repo = new UpdateRepository(conn, gson);
    Update u = gson.fromJson("{\"changes\":{\"rs.ltt.jmap.common.entity.Email\":{\"created\":[],\"updated\":[\"3709b25f-f1d4-421d-ae07-dc2d2e8915f3\",\"47988266-7eaf-49f7-8c49-6f73b4c86d81\"]},\"rs.ltt.jmap.common.entity.Mailbox\":{\"created\":[],\"updated\":[\"2fbbe51a-ac62-4d2f-a258-8e6792bde83a\"]},\"rs.ltt.jmap.common.entity.Thread\":{\"created\":[],\"updated\":[]}},\"newVersion\":\"1\",\"state\":\"0\"}", Update.class);

    repo.insert("2d894a70-35ef-43f7-9dbb-31e109b24564", "0", u);

    Map res = r.table("update")
                            .getAll("2d894a70-35ef-43f7-9dbb-31e109b24564")
                            .optArg("index","account")
                            .without("account")
                            .filter(row -> row.g("state").eq("0"))
                            .run(conn, Map.class).first();

    res.replace("id", res.get("state"));

    Assertions.assertEquals(gson.toJson(u, Update.class), gson.toJson(gson.fromJson(gson.toJson(res), Update.class)));
  }
}

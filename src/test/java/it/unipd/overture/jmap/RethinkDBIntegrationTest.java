package it.unipd.overture.jmap;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RethinkDBIntegrationTest {

  private static final RethinkDB r = RethinkDB.r;

  @Test
  void testRethinkDBContainer() {
    try (GenericContainer<?> rethinkDBContainer = new GenericContainer<>("rethinkdb:latest")
        .withExposedPorts(28015)
        .waitingFor(Wait.forLogMessage(".*Server ready.*\\n", 1))) {

      rethinkDBContainer.start();

      // Retrieve connection details
      String host = rethinkDBContainer.getContainerIpAddress();
      Integer port = rethinkDBContainer.getMappedPort(28015);

      try (Connection connection = r.connection().hostname(host).port(port).connect()) {
        // Use the RethinkDB driver to perform a simple operation
        r.db("test").tableCreate("test_table").run(connection);

        // Insert a document
        var inputDB = "{\"name\":\"John\",\"age\":30}";
        var insertDB = r.db("test").table("test_table").insert(r.json(inputDB)).toJson().run(connection);

        JsonObject objJson = new Gson().fromJson(insertDB.first().toString(), JsonObject.class);
        var key = objJson.get("generated_keys").getAsString();

        // Query the inserted document
        boolean qTableExists = !r.db("test")
            .table("test_table")
            .get(key)
            .run(connection)
            .<Long>toList()
            .isEmpty();

        // Assert that the document was successfully inserted
        assertTrue(qTableExists, "Document should exist in the table.");

        var qTableContent = r.db("test").table("test_table").get(key).without("id").toJson().run(connection)
            .first().toString();
        JsonObject resultJson = new Gson().fromJson(qTableContent, JsonObject.class);
        JsonObject inputJson = new Gson().fromJson(inputDB, JsonObject.class);
        assertEquals(inputJson, resultJson);
      }
    } catch (Exception e) {
      e.printStackTrace(); // Handle exceptions appropriately in your tests
      fail("Test failed with an exception: " + e.getMessage());
    }
  }
}

package it.unipd.overture.adapters.out;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlRuntimeError;
import com.rethinkdb.model.OptArgs;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;
import com.rethinkdb.utils.Types;

// Examples of driver usage: https://github.com/rethinkdb/rethinkdb-java/blob/master/src/test/java/com/rethinkdb/RethinkDBTest.java

public class RethinkDBConnection {
  private Connection conn;
  private final TypeReference<List<String>> stringList = Types.listOf(String.class);
  private final TypeReference<Map<String, Object>> stringObjectMap = Types.mapOf(String.class, Object.class);

  Database(String host, int port, String db) {
    conn = RethinkDB.r.connection().hostname(host).port(port).connect().use(db);
  }

  Connection getConnection() {
    return conn;
  }
}

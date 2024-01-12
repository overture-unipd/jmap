package it.unipd.overture.jmap;

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

public class Database {
  private RethinkDB r;
  private Connection conn;
  private Gson gson;
  private String db;
  private final TypeReference<List<String>> stringList = Types.listOf(String.class);
  private final TypeReference<Map<String, Object>> stringObjectMap = Types.mapOf(String.class, Object.class);

  Database(String host, int port, String db) {
    this.r = RethinkDB.r;
    this.db = db;
    this.conn = this.r.connection().hostname(host).port(port).connect().use(db);
    this.gson = new Gson();
  }

  Database() {
    this(
      System.getenv("DB_HOST"),
      Integer.parseInt(System.getenv("DB_PORT")),
      System.getenv("DB_NAME")
    );
  }

  public void reset(LinkedList<String[]> accounts, String domain) {
    try {
      r.dbDrop(db).run(conn);
    } catch (ReqlRuntimeError e) {
    }

    r.dbCreate(db).run(conn);

    r.tableCreate("account").run(conn);
    r.table("account").indexCreate("address").run(conn);
    for (var acc : accounts) {
      r.table("account").insert(
        r.hashMap("address", acc[0]+"@"+domain)
          .with("name", acc[0])
          .with("password", acc[1])
          .with("state", "0")
      ).run(conn);
    }

    r.tableCreate("email").run(conn);
    r.tableCreate("mailbox").run(conn);
    r.tableCreate("attachment").run(conn);
    r.tableCreate("update").run(conn);
  }

  public String getAccountName(String id) {
    return r.table("account").get(id).run(conn, Properties.class).single().getProperty("name");
  }

  public String getAccountAddress(String id) {
    return r.table("account").get(id).run(conn, Properties.class).single().getProperty("address");
  }

  public String getAccountPassword(String id) {
    var t = r.table("account").get(id).run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("password");
  }

  public String getAccountId(String address) {
    var t = r.table("account").getAll(address).optArg("index", "address").run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("id");
  }

  public String getAccountState(String accountid) {
    Properties t = r.table("account").get(accountid).run(conn, Properties.class).single();
    return t.getProperty("state");
  }

  public void incrementAccountState(String accountid) {
    r.table("account").get(accountid).update(
        a -> r.hashMap("state", a.g("state").coerceTo("number").add(1))
    ).run(conn);
  }

  public byte[] getAttachment(String id) {
    // Object cursor = r.table("attachment").get(id).pluck("content").toJson().run(conn, OptArgs.of("binary_format", "raw")).single();
    var cursor = r.table("attachment").get(id).pluck("content").toJson().run(conn);
    var blob = gson.fromJson(cursor.first().toString(), JsonObject.class)
      .getAsJsonObject("content")
      .get("data")
      .getAsString();
      return Base64.getDecoder().decode(blob.getBytes());
  }

  public String insertAttachment(byte[] content) {
    var key = r.table("attachment").insert(
      r.hashMap("content", content)
    ).toJson().run(conn);
    return gson.fromJson(key.first().toString(), JsonObject.class).get("generated_keys").getAsString();
  }

  public List<String> getTable(String table) {
    Result<Map<String, Object>> cursor = r.table(table).run(conn, stringObjectMap);
    List<String> res = new LinkedList<>();
    for (var el : cursor) {
      res.add(gson.toJson(el));
    }
    cursor.close();
    return res;
  }

  public String insertInTable(String table, String content) {
    Map<String, Object> res = r.table(table).insert(r.json(content)).run(conn, stringObjectMap).single();
    var keys = ((List<?>) res.get("generated_keys"));
    if (keys != null) {
      return keys.get(0).toString();
    }
    return null;
  }

  public String replaceInTable(String table, String id, String content) {
    Map<String, Object> res = r.table(table).get(id).replace(r.json(content)).run(conn, stringObjectMap).single();
    var keys = ((List<?>) res.get("generated_keys"));
    if (keys != null) {
      return keys.get(0).toString();
    }
    return null;
  }
}

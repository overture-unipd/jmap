package it.unipd.overture.jmap;

import java.util.Base64;
import java.util.LinkedList;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlRuntimeError;
import com.rethinkdb.model.OptArgs;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

public class Database {
  RethinkDB r;
  Connection conn;
  Gson gson;
  String db;

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

  public void reset(String domain, LinkedList<String[]> accounts) {
    try {
      r.dbDrop(db).run(conn);
    } catch (ReqlRuntimeError e) {
    }

    r.dbCreate(db).run(conn);

    r.tableCreate("account").run(conn);
    r.table("account").indexCreate("address").run(conn);
    for (var acc : accounts) {
      r.table("account").insert(
        r.hashMap("address", acc[0]+"@"+domain).with("password", acc[1]).with("state", "0")
      ).run(conn);
    }

    r.tableCreate("email").run(conn);

    r.tableCreate("thread").run(conn);

    r.tableCreate("mailbox").run(conn);

    r.tableCreate("file").run(conn);
  }

  public String getAccountPassword(String id) {
    var t = r.table("account").get(id).run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("password");
  }

  public void incrementAccountState(String accountid) {
    r.table("account").get(accountid).update(
        a -> r.hashMap("state", a.g("state").add(1))
    ).run(conn);
  }

  public String getEmail(String id) {
    var t = r.table("email").get("id").toJson().run(conn);
    return t.toString();
  }

  public String getAccountEmails(String id) {
    var t = r.table("email").get(id).toJson().run(conn);
    return t.toString();
  }

  public String getAccountState(String accountid) {
    Properties t = r.table("account").get(accountid).run(conn, Properties.class).single();
    return t.getProperty("state");
  }

  public void insertEmail(String accountid, Object obj) {
    var t = r.table("email").insert(obj).run(conn);
    System.out.println(t);
  }

  public String insertFile(byte[] content) {
    var key = r.table("file").insert(
      r.hashMap("content", content)
    ).toJson().run(conn);
    return gson.fromJson(key.first().toString(), JsonObject.class).get("generated_keys").getAsString();
  }

  public String insertFile(String id, byte[] content) {
    var key = r.table("file").insert(
      r.hashMap("content", content)
    ).toJson().run(conn);
    return gson.fromJson(key.first().toString(), JsonObject.class).get("generated_keys").getAsString();
  }

  public byte[] getFile(String id) {
    // Object cursor = r.table("file").get(id).pluck("content").toJson().run(conn, OptArgs.of("binary_format", "raw")).single();
    var cursor = r.table("file").get(id).pluck("content").toJson().run(conn);
    var blob = gson.fromJson(cursor.first().toString(), JsonObject.class)
      .getAsJsonObject("content")
      .get("data")
      .getAsString();
      return Base64.getDecoder().decode(blob.getBytes());
  }

  public String getAccountId(String address) {
    var t = r.table("account").getAll(address).optArg("index", "address").run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("id");
  }

  public String createEmail(String accountid, String email) {
    var id = r.table("email").insert(r.json(email)).run(conn);
    return id.toString();
  }
}

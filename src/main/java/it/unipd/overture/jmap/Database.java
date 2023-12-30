package it.unipd.overture.jmap;

import java.util.Base64;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlRuntimeError;
import com.rethinkdb.net.Connection;

public class Database {
  Configuration conf;
  RethinkDB r;
  Connection conn;
  Gson gson;

  Database() {
    this.conf = new Configuration();
    this.r = RethinkDB.r;
    this.conn = this.r.connection().hostname(conf.getDatabase()).port(28015).connect().use(this.conf.getDbName());
    this.gson = new Gson();
  }

  public void reset() {
    try {
      r.dbDrop(conf.getDbName()).run(conn);
      // r.dbDrop(conf.getDomain()).run(conn);
    } catch (ReqlRuntimeError e) {
    }

    r.dbCreate(conf.getDbName()).run(conn);
    // r.dbCreate(conf.getDomain()).run(conn);

    r.tableCreate("account").run(conn);
    r.table("account").indexCreate("address").run(conn);
    for (var acc : conf.getAccounts()) {
      r.table("account").insert(
        r.hashMap("address", acc.getAddress()+"@"+conf.getDomain()).with("password", acc.getPassword()).with("state", r.uuid())
      ).run(conn);
    }

    r.tableCreate("mail").run(conn);
    r.table("mail").indexCreate("account_id").run(conn);

    r.tableCreate("file").run(conn);
  }

  public String getAccountPassword(String id) {
    var t = r.table("account").get(id).run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("password");
    // r.table("account").getAll(address).g("address").run(conn).first().toString(); // select only the password
  }

  public void updateAccountState(String accountid, String state) {
    r.table("account").get(accountid).update(r.hashMap("state", state)).run(conn);
  }

  public void getMail(String mailid) {
    System.out.println(r.table("mail").get(mailid).toJson().run(conn));
  }

  public void insertMail(String accountid, Object obj) {
    var t = r.table("mail").insert(obj).run(conn);
    System.out.println(t);
  }

  public String insertFile(byte[] content) {
    var id = r.table("file").insert(
      r.hashMap("content", content)
    ).toJson().run(conn);
    return gson.fromJson(id.first().toString(), JsonObject.class).get("generated_keys").getAsString();
  }

  public byte[] getFile(String id) {
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

  public String getAccountState(String id) {
    var t = r.table("account").get(id).run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("state");
  }

  public String getAccountMails(String accountid) {
    return r.table("account").getAll(accountid).run(conn).toString(); // get all emails
  }
}

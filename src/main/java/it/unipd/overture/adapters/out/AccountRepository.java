package it.unipd.overture.adapters.out;

import java.util.Properties;

import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.ports.out.AccountPort;

public class AccountRepository implements AccountPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;

  @Inject
  AccountRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String getId(String username) {
    Properties t = r.table("account").getAll(username).optArg("index", "username").run(conn, Properties.class).single();
    return t.getProperty("id");
    // return t.getProperty(u); // gson.fromJson(gson.toJson(id), Properties.class).getProperty("id");
  }

  @Override
  public String getPassword(String id) {
    return r.table("account").get(id).run(conn, Properties.class).single().getProperty("password");
  }

  @Override
  public String getState(String id) {
    Properties t = r.table("account").get(id).run(conn, Properties.class).single();
    return t.getProperty("state");
  }

  @Override
  public void incrementState(String id) {
    r.table("account").get(id).update(
      a -> r.hashMap("state", a.g("state").coerceTo("number").add(1))
    ).run(conn);
  }
}

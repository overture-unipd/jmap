package it.unipd.overture.adapter.out;

import java.util.Map;

import com.google.inject.Inject;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.port.out.StatePort;

public class StateRepository implements StatePort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;

  @Inject
  StateRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String get(String accountid) {
    Result<Map> res = r.table("account").get(accountid).run(conn, Map.class);
    Map t = res.next();
    if (t == null) return null;
    return t.get("state").toString();
  }

  @Override
  public void increment(String accountid) {
    r.table("account").get(accountid).update(
      a -> r.hashMap("state", a.g("state").coerceTo("number").add(1))
    ).run(conn);
  }
}

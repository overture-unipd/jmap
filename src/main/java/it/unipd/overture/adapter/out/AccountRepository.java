package it.unipd.overture.adapter.out;

import java.util.Map;
import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.port.out.AccountPort;

public class AccountRepository implements AccountPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;

  @Inject
  AccountRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String getId(String username) {
    Result<Map> res = r.table("account").getAll(username).optArg("index", "username").run(conn, Map.class);
    if (!res.hasNext()) return null;
    return res.first().get("id").toString();
  }

  @Override
  public String getPassword(String id) {
    Result<Map> res = r.table("account").get(id).run(conn, Map.class);
    Map t = res.next();
    if (t == null) return null;
    return t.get("password").toString();
  }
}

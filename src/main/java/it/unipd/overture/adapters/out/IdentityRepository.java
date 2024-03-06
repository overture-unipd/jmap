package it.unipd.overture.adapters.out;

import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.ports.out.IdentityPort;

public class IdentityRepository implements IdentityPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;

  @Inject
  IdentityRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String getAll(String accountid) {
    return r.table("identity").getAll(accountid).optArg("index", "account").coerceTo("array").toJson().run(conn).single().toString();
  }

  @Override
  public String getFirst(String accountid) {
    return r.table("identity").getAll(accountid).optArg("index", "account").nth(0).toJson().run(conn).single().toString();
  }
}

package it.unipd.overture.adapters.out;

import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.ports.out.IdentityPort;

public class IdentityImpl implements IdentityPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;

  @Inject
  IdentityImpl(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String getIdentities(String accountid) {
    return r.table("identity").getAll(accountid).optArg("index", "account").coerceTo("array").toJson().run(conn).single().toString();
  }

  @Override
  public String getFirstIdentity(String accountid) {
    return r.table("identity").getAll(accountid).optArg("index", "account").nth(0).toJson().run(conn).single().toString();
  }
}

package it.unipd.overture.adapters.out;

import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.ports.out.ThreadPort;

public class ThreadImpl implements ThreadPort {
  private Connection conn;
  private final RethinkDB r = RethinkDB.r;

  @Inject
  ThreadImpl(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String getThread(String id) {
    return r.table("email").getAll(id).optArg("index", "threadId").coerceTo("array").toJson().run(conn).single().toString();
  }
}

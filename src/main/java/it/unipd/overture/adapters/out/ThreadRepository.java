package it.unipd.overture.adapters.out;

import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.ports.out.ThreadPort;

public class ThreadRepository implements ThreadPort {
  private Connection conn;
  private final RethinkDB r = RethinkDB.r;

  @Inject
  ThreadRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String get(String id) {
    return r.table("email").getAll(id).optArg("index", "threadId").coerceTo("array").toJson().run(conn).single().toString();
  }
}

package it.unipd.overture.adapter.out;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.port.out.ThreadPort;

public class ThreadRepository implements ThreadPort {
  private Connection conn;
  private final RethinkDB r = RethinkDB.r;

  @Inject
  ThreadRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public List<String> getOf(String accountid, String threadid) {
    List<String> ids = new ArrayList<>();
    Result<String> res = r.table("email").getAll(accountid).optArg("index","account").filter(row -> row.g("threadId").eq(threadid)).g("id").run(conn, String.class);
    if (! res.hasNext()) return null;
    res.forEach(doc -> ids.add(doc));
    return ids;
  }
}

package it.unipd.overture;

import com.google.inject.Provides;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

// Examples of driver usage: https://github.com/rethinkdb/rethinkdb-java/blob/master/src/test/java/com/rethinkdb/RethinkDBTest.java

public class RethinkDBConnection {
  private Connection conn;

  public RethinkDBConnection(String host, Integer port, String db) {
    this.conn = RethinkDB.r.connection().hostname(host).port(port).connect().use(db);
  }

  @Provides
  public Connection provideConnection() {
    return conn;
  }
}

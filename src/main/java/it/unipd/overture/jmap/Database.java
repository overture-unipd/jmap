package it.unipd.overture.jmap;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlError;
import com.rethinkdb.gen.exc.ReqlRuntimeError;
import com.rethinkdb.gen.exc.ReqlQueryLogicError;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Connection;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.UUID;
import rs.ltt.jmap.common.entity.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Database {
    RethinkDB r;
    Argon2PasswordEncoder encoder;

    Database() {
        this.r = RethinkDB.r;
        this.encoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
    }

    public Connection connect() {
        return r.connection().hostname("storage").port(28015).connect().use("jmap");
    }

    public void reset() {
        Connection conn = this.connect();

        try {
            r.dbDrop("jmap").run(conn);
        } catch (ReqlRuntimeError e) {}

        r.dbCreate("jmap").run(conn);

        r.tableCreate("account").optArg("primary_key", "address").run(conn);
        r.table("account").indexCreate("id").run(conn);
        r.table("account").insert(r.array(
            r.hashMap("address", "admin@localhost").with("password", this.encoder.encode("admin")).with("id", UUID.randomUUID().toString()),
            r.hashMap("address", "alice@localhost").with("password", this.encoder.encode("alice")).with("id", UUID.randomUUID().toString()),
            r.hashMap("address", "bob@localhost").with("password", this.encoder.encode("bob")).with("id", UUID.randomUUID().toString())
        )).run(conn);

        r.tableCreate("bearer").run(conn);
        r.table("bearer").indexCreate("account_id").run(conn);

        r.tableCreate("mail").run(conn);
        r.table("mail").indexCreate("account_id").run(conn);
    }

    public String login(String address, String password) {
        Connection conn = this.connect();
        if (this.encoder.matches(password, r.table("account").get(address).g("password").run(conn).first().toString())) {
            return r.table("account").get(address).g("id").run(conn).first().toString();
        }
        return null;
    }

    public void insertMail(String jsonRepr) {
        Connection conn = this.connect();
        r.table("mail").insert(
            new Gson().fromJson(jsonRepr, Email.class)
        ).run(conn);
    }

    public String getMail(String bearer, String mailid) {
        Connection conn = this.connect();
        return r.table("mail").get(mailid).filter(r.hashMap("account_id", bearer)).run(conn).first().toString();
    }

    public String getAccountMails(String bearer) {
        Connection conn = this.connect();
        return r.table("mail").getAll(bearer).optArg("index", "account_id").run(conn).toString();
    }

    public String getAdminBearer() {
        Connection conn = this.connect();
        return r.table("account").get("admin@localhost").g("id").run(conn).first().toString();
    }
}

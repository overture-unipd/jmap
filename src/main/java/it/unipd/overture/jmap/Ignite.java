package it.unipd.overture.jmap;

import spark.Redirect;
import spark.utils.IOUtils;

import static spark.Spark.halt;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.before;
import static spark.Spark.redirect;

import java.io.ByteArrayInputStream;

public class Ignite {
  public static void main(String[] args) {
    port(8000);

    get("/", (q, a) -> "Yeah I'm up");

    redirect.get("/.well-known/jmap", "/api/jmap", Redirect.Status.MOVED_PERMANENTLY);

    before("/api/*", (q, a) -> {
      var auth = q.headers("Authorization");
      if (auth == null) {
        halt(401, "Requests have to be authenticated.");
      }
      var dispatcher = new Dispatcher(new Database());
      var creds = dispatcher.extractAuth(auth);
      var address = creds[0];
      var password = creds[1];
      if (! dispatcher.authenticate(address, password)) {
        halt(401, "Wrong credentials.");
      }
    });

    get("/api/jmap", (q, a) -> {
      a.type("application/json");
      var dispatcher = new Dispatcher(new Database());
      var address = dispatcher.extractAuth(q.headers("Authorization"))[0];
      return dispatcher.session(address);
    });

    post("/api/jmap", (q, a) -> {
      a.type("application/json");
      var dispatcher = new Dispatcher(new Database());
      var address = dispatcher.extractAuth(q.headers("Authorization"))[0];
      return dispatcher.jmap(address, q.body());
    });

    post("/api/upload", (q, a) -> {
      a.type("application/json");
      var type = q.headers("Content-Type");
      var size = q.contentLength();
      var blob = q.bodyAsBytes();
      var dispatcher = new Dispatcher(new Database());
      return dispatcher.upload(type, size, blob);
    });


    get("/api/download", (q, a) -> {
      // TODO: should really be:   "downloadUrl": "https://www.fastmailusercontent.com/jmap/download/{accountId}/{blobId}/{name}?type={type}",
      var dispatcher = new Dispatcher(new Database());
      var blobid = q.queryParams("blobid");
      var blob = dispatcher.download(blobid);
      if (blob == null) {
        halt(500, "Something went wrong with the download.");
      }

      var is = new ByteArrayInputStream(blob);
      var os = a.raw().getOutputStream();
      IOUtils.copy(is, os);
      is.close();
      os.close();

      a.type("application/octet-stream");
      // a.header("Content-Disposition", String.format("attachment; filename=\"%s.raw\"", blobid));

      return null;
    });

    get("/reset", (q, a) -> new Dispatcher(new Database()).reset());
  }
}

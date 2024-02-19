package it.unipd.overture.adapters.in;

import spark.Redirect;
import spark.utils.IOUtils;
import static spark.Spark.after;
import static spark.Spark.afterAfter;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.redirect;

import java.io.ByteArrayInputStream;

public class Request {
  private final RequestPort request;

  RequestHandler(Request request) {
    this.request = request;
  }

  String wellKnown() {
    return "/api/jmap";
  }

  String session(String json) {
  }

  String postJmap(String id) {

  }

  byte[] download(String id) {

  }

  void upload(Byte[] data) {

  }

  public void main() {
    var dispatcher = new Dispatcher();
    dispatcher.reset();
  }


  public static void main(String[] args) {

    port(8000);

    get("/", (q, a) -> "Yeah I'm up");

    get("/.well-known/jmap", (q, a) -> {
      a.header("Access-Control-Allow-Methods", "GET, POST");
      a.header("Access-Control-Allow-Origin", "*");
      a.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
      a.redirect("/api/jmap");// , Redirect.Status.MOVED_PERMANENTLY);
      return "";
    });

    // redirect.get("/.well-known/jmap", "/api/jmap", Redirect.Status.FOUND);
    // redirect.get("/.well-known/jmap", "/api/jmap", Redirect.Status.MOVED_PERMANENTLY);
    // redirect.post("/.well-known/jmap", "/api/jmap", Redirect.Status.MOVED_PERMANENTLY);
    // redirect.options("/.well-known/jmap", "/api/jmap", Redirect.Status.MOVED_PERMANENTLY);
    // redirect.any("/.well-known/jmap", "/api/jmap", Redirect.Status.MOVED_PERMANENTLY);

    options("*", (q, a) -> {
      return "";
    });

    afterAfter((q, a) -> {
      a.header("Access-Control-Allow-Methods", "GET, POST");
      a.header("Access-Control-Allow-Origin", "*");
      a.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
    });

    before("/api/*", (q, a) -> {
      if (q.requestMethod() != "OPTIONS") {

      var auth = q.headers("Authorization");
      if (auth == null) {
        halt(401, "Requests have to be authenticated.");
      }
      String[] creds = dispatcher.extractAuth(auth);
      if (creds.length < 2) {
        halt(401, "Client Error");
      }
      var address = creds[0];
      var password = creds[1];
      if (! dispatcher.authenticate(address, password)) {
        halt(401, "Wrong credentials.");
      }
      }
    });

    get("/api/jmap", (q, a) -> {
      a.type("application/json");
      var address = dispatcher.extractAuth(q.headers("Authorization"))[0];
      return dispatcher.session(address);
    });

    post("/api/jmap", (q, a) -> {
      a.type("application/json");
      var address = dispatcher.extractAuth(q.headers("Authorization"))[0];
      return dispatcher.jmap(address, q.body());
    });

    post("/api/upload", (q, a) -> {
      a.type("application/json");
      var type = q.headers("Content-Type");
      var size = q.contentLength();
      var blob = q.bodyAsBytes();
      return dispatcher.upload(type, size, blob);
    });


    get("/api/download", (q, a) -> {
      // TODO: should really be: "downloadUrl": "https://www.fastmailusercontent.com/jmap/download/{accountId}/{blobId}/{name}?type={type}",
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

    get("/reset", (q, a) -> dispatcher.reset());
  }
}

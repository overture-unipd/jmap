package it.unipd.overture.adapters.in;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.google.inject.Inject;
import it.unipd.overture.ports.in.AuthenticationPort;
import it.unipd.overture.ports.in.SessionPort;
import it.unipd.overture.ports.in.MethodPort;
import it.unipd.overture.ports.in.UploadPort;
import it.unipd.overture.ports.in.DownloadPort;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

public class Spark {
  private final AuthenticationPort authentication;
  private final SessionPort session;
  private final MethodPort method;
  private final UploadPort upload;
  private final DownloadPort download;

  @Inject
  Spark(
      AuthenticationPort authentication,
      SessionPort session,
      MethodPort method,
      UploadPort upload,
      DownloadPort download
  ) {
    this.authentication = authentication;
    this.session = session;
    this.method = method;
    this.upload = upload;
    this.download = download;
  }

  private String authenticate(Request q, Response a) {
    if (! authentication.authenticate(q.headers("Authorization"))) {
      halt(401, "Wrong credentials.");
    }
    return null;
  }

  private String up(Request q, Response a) {
    return "Vivo, ma non ho scelta né un motivo.";
  }

  private String wellKnown(Request q, Response a) {
    a.redirect("/api/jmap");// , Redirect.Status.MOVED_PERMANENTLY);
    return null;
  }

  private String getJmap(Request q, Response a) {
    a.type("application/json");
    return session.get(q.headers("Authorization"));
  }

  private String postJmap(Request q, Response a) {
    a.type("application/json");
    return method.get(q.body());
  }

  private String download(Request q, Response a) {
    a.type("application/octet-stream");
    var blobid = q.queryParams("blobid");
    var blob = download.pull(blobid);
    if (blob == null) {
      halt(500, "Something went wrong with the download.");
    }
    try {
      var is = new ByteArrayInputStream(blob);
      var os = a.raw().getOutputStream();
      IOUtils.copy(is, os);
      is.close();
      os.close();
    } catch (IOException e) {
      a.body("Error");
    }
    return null;
  }

  private String upload(Request q, Response a) {
    a.type("application/json");
    var blob = q.bodyAsBytes();
    return upload.push(blob);
  }

  public void start() {
    port(8000);

    before("/api/*", this::authenticate);
    get("/", this::up);
    get("/.well-known/jmap", this::wellKnown);
    get("/api/jmap", this::getJmap);
    post("/api/jmap", this::postJmap);
    get("/api/upload", this::upload);
    get("/api/download", this::download);
  }
}

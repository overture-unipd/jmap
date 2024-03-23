package it.unipd.overture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import io.minio.MinioClient;
import it.unipd.overture.adapter.out.AccountRepository;
import it.unipd.overture.adapter.out.AttachmentRepository;
import it.unipd.overture.adapter.out.EmailRepository;
import it.unipd.overture.adapter.out.EmailSubmissionRepository;
import it.unipd.overture.adapter.out.IdentityRepository;
import it.unipd.overture.adapter.out.MailboxRepository;
import it.unipd.overture.adapter.out.StateRepository;
import it.unipd.overture.adapter.out.ThreadRepository;
import it.unipd.overture.adapter.out.UpdateRepository;
import it.unipd.overture.controller.AttachmentController;
import it.unipd.overture.controller.AuthenticationController;
import it.unipd.overture.controller.MethodController;
import it.unipd.overture.controller.SessionController;
import it.unipd.overture.port.in.AuthenticationPort;
import it.unipd.overture.port.in.DownloadPort;
import it.unipd.overture.port.in.MethodPort;
import it.unipd.overture.port.in.SessionPort;
import it.unipd.overture.port.in.UploadPort;
import it.unipd.overture.port.out.AccountPort;
import it.unipd.overture.port.out.AttachmentPort;
import it.unipd.overture.port.out.EmailPort;
import it.unipd.overture.port.out.EmailSubmissionPort;
import it.unipd.overture.port.out.IdentityPort;
import it.unipd.overture.port.out.MailboxPort;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.port.out.ThreadPort;
import it.unipd.overture.port.out.UpdatePort;
import rs.ltt.jmap.gson.JmapAdapters;

public class BindModule extends AbstractModule {
  @Override
  public void configure() {
    // in
    bind(AuthenticationPort.class).to(AuthenticationController.class);
    bind(DownloadPort.class).to(AttachmentController.class);
    bind(MethodPort.class).to(MethodController.class);
    bind(SessionPort.class).to(SessionController.class);
    bind(UploadPort.class).to(AttachmentController.class);

    // out
		bind(AccountPort.class).to(AccountRepository.class);
		bind(AttachmentPort.class).to(AttachmentRepository.class);
		bind(EmailPort.class).to(EmailRepository.class);
		bind(EmailSubmissionPort.class).to(EmailSubmissionRepository.class);
    bind(UpdatePort.class).to(UpdateRepository.class);
    bind(IdentityPort.class).to(IdentityRepository.class);
    bind(MailboxPort.class).to(MailboxRepository.class);
    bind(StatePort.class).to(StateRepository.class);
    bind(ThreadPort.class).to(ThreadRepository.class);

    bind(String.class)
      .annotatedWith(Names.named("MINIO_BUCKET"))
      .toInstance(System.getenv("MINIO_BUCKET"));
  }

  @Provides
  public Connection provideConnection() {
    return RethinkDB.r.connection().hostname(
        System.getenv("RETHINKDB_HOST"))
                .port(Integer.parseInt(System.getenv("RETHINKDB_PORT")))
                .connect()
                .use(System.getenv("RETHINKDB_DB"));
  }

  @Provides
  public MinioClient provideMinioClient() {
    return MinioClient.builder()
      .endpoint(System.getenv("MINIO_HOST"), Integer.parseInt(System.getenv("MINIO_PORT")), false)
      .credentials(System.getenv("MINIO_ACCESS"), System.getenv("MINIO_SECRET"))
      .build();    
  }
  
  @Provides
  public Gson provideGson() {
    var gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    gsonBuilder.registerTypeAdapter(Update.class, new UpdateSerializer());
    gsonBuilder.registerTypeAdapter(Update.class, new UpdateDeserializer());
    return gsonBuilder.disableHtmlEscaping().create();
  }
}

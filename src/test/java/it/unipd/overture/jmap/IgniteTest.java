package it.unipd.overture.jmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import spark.Request;
import spark.Response;

class AppTest {
  @Mock
  private Database database;

  @Mock
  private Request request;

  @Mock
  private Response response;

  @InjectMocks
  private App app;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    Mockito.reset(request, response, database);
  }

  @Test
  void appHasAGreeting() {
    assertNotNull(app.getGreeting(), "app should have a greeting");
  }

  @Test
  void testLoginSuccesful() {
    when(request.queryParams("username")).thenReturn("user@localhost");
    when(request.queryParams("password")).thenReturn("$argon2id$v=19$m=60000,t=10,p=1$0g7qiiqJJXOakd3UYPKrpA$fiLZPqYlxuIkPdYj1rVSoNkbO4aJkRO4FSd1SNh0iiY");
    when(database.login("user@localhost", "$argon2id$v=19$m=60000,t=10,p=1$0g7qiiqJJXOakd3UYPKrpA$fiLZPqYlxuIkPdYj1rVSoNkbO4aJkRO4FSd1SNh0iiY")).thenReturn("fakebearer");
    when(request.session().attribute("bearer")).thenReturn("bearer");

    String result = app.login(request, response);
    assertEquals("fakebearer", request.session().attribute("bearer"));
    verify(request, times(1)).queryParams("username");
    verify(request, times(1)).queryParams("password");
    verify(database, times(1)).login("user@localhost", "$argon2id$v=19$m=60000,t=10,p=1$0g7qiiqJJXOakd3UYPKrpA$fiLZPqYlxuIkPdYj1rVSoNkbO4aJkRO4FSd1SNh0iiY");
    assertEquals("", result);
  }

  @Test
  void testLoginFailed() {
    when(request.queryParams("username")).thenReturn("bob@localhost");
    when(request.queryParams("password")).thenReturn("$argon2id$v=19$m=60000,t=10,p=1$0g7qiiqJJXOakd3UYPKrpA$fiLZPqYlxuIkPdYj1rVSoNkbO4aJkRO4FSd1SNh0iiY");
    when(database.login("bob@localhost", "$argon2id$v=19$m=60000,t=10,p=1$0g7qiiqJJXOakd3UYPKrpA$fiLZPqYlxuIkPdYj1rVSoNkbO4aJkRO4FSd1SNh0iiY")).thenReturn(null);

    String result = app.login(request, response);
    verify(request, times(1)).queryParams("username");
    verify(request, times(1)).queryParams("password");
    verify(database, times(1)).login("bob@localhost", "$argon2id$v=19$m=60000,t=10,p=1$0g7qiiqJJXOakd3UYPKrpA$fiLZPqYlxuIkPdYj1rVSoNkbO4aJkRO4FSd1SNh0iiY");
    assertEquals("", result);
  }

  @Test
  void testGetMailSuccesful() {
    when(request.headers("Authorization")).thenReturn("df3226b7-98cf-4fec-926c-427c41fdc95a");
    when(request.queryParams("id")).thenReturn("mockedMailId_1");
    when(database.getMail("df3226b7-98cf-4fec-926c-427c41fdc95a", "mockedMailId_1")).thenReturn("mockedMailJson");

    String result = app.getMail(request, response);

    verify(request, times(1)).headers("Authorization");
    verify(request, times(1)).queryParams("id");
    verify(database, times(1)).getMail("df3226b7-98cf-4fec-926c-427c41fdc95a", "mockedMailId_1");
    assertEquals("mockedMailJson", result);
  }

  @Test
  void testGetMailFailed() {
    when(request.headers("Authorization")).thenReturn("df3226b7-98cf-4fec-926c-427c41fdc95s");
    when(request.queryParams("id")).thenReturn("mockedMailId_1");
    when(database.getMail("df3226b7-98cf-4fec-926c-427c41fdc95s", "mockedMailId_1")).thenReturn("");

    String result = app.getMail(request, response);

    verify(request, times(1)).headers("Authorization");
    verify(request, times(1)).queryParams("id");
    verify(database, times(1)).getMail("df3226b7-98cf-4fec-926c-427c41fdc95s", "mockedMailId_1");
    assertEquals("", result);
  }
}

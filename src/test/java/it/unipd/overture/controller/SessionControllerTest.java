package it.unipd.overture.controller;

import com.google.gson.Gson;

import it.unipd.overture.service.SessionLogic;
import rs.ltt.jmap.common.SessionResource;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.OngoingStubbing;

public class SessionControllerTest {
    @Mock SessionLogic sessionLogic;
    @Mock Gson gson;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getTest() {
        String auth = "Token: YWxpY2U6YWw=";
        String username = "alice";
        SessionController sessionController = new SessionController(gson, sessionLogic);
        OngoingStubbing<SessionResource> stubbing = when(sessionLogic.get(Mockito.any(String.class)));
        SessionResource sessionResource = SessionResource.builder().username(username).build();
        stubbing.thenReturn(sessionResource);
        when(gson.toJson(Mockito.any(SessionResource.class))).thenReturn(sessionResource.getUsername());
        Assertions.assertEquals(username, sessionController.get(auth));
    }
}

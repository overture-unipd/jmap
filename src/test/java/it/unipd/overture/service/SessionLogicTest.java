package it.unipd.overture.service;

import it.unipd.overture.port.out.AccountPort;
import it.unipd.overture.port.out.StatePort;
import it.unipd.overture.service.SessionLogic;
import rs.ltt.jmap.common.SessionResource;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class SessionLogicTest {
    @Mock AccountPort accountPort;
    @Mock StatePort statePort;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getTest() {
        String username = "username";
        String id = "id";
        SessionLogic sessionLogic = new SessionLogic(accountPort, statePort);
        when(accountPort.getId(username)).thenReturn(id);
        SessionResource sessionResource = sessionLogic.get(username);
        Assertions.assertEquals(username, sessionResource.getUsername());
    }
}

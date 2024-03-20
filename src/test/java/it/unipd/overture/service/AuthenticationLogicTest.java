package it.unipd.overture.service;

import it.unipd.overture.port.out.AccountPort;
import it.unipd.overture.service.AuthenticationLogic;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class AuthenticationLogicTest {
    @Mock AccountPort accountPort;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void authenticateTest() {
        String username = "username";
        String id = "id";
        String password = "password";
        AuthenticationLogic authenticationLogic = new AuthenticationLogic(accountPort);
        when(accountPort.getId(username)).thenReturn(id);
        when(accountPort.getPassword(id)).thenReturn(password);
        Assertions.assertTrue(authenticationLogic.authenticate(username, password));
    }

    @Test
    public void authenticateTestWrongUsername() {
        String username = "usernam";
        String id = "id";
        String password = "password";
        AuthenticationLogic authenticationLogic = new AuthenticationLogic(accountPort);
        when(accountPort.getId("username")).thenReturn(null);
        when(accountPort.getPassword(id)).thenReturn(password);
        Assertions.assertFalse(authenticationLogic.authenticate(username, password));
    }

    @Test
    public void authenticateTestWrongPassword() {
        String username = "username";
        String id = "id";
        String password = "passwor";
        AuthenticationLogic authenticationLogic = new AuthenticationLogic(accountPort);
        when(accountPort.getId("username")).thenReturn(id);
        when(accountPort.getPassword(id)).thenReturn("password");
        Assertions.assertFalse(authenticationLogic.authenticate(username, password));
    }
}

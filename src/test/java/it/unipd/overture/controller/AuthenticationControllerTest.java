package it.unipd.overture.controller;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unipd.overture.controller.AuthenticationController;
import it.unipd.overture.service.AuthenticationLogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest {
    @Mock AuthenticationLogic authenticationLogic;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void authenticateTest() {
        String auth = "Token: YWxpY2U6YWw=";
        AuthenticationController authenticationController = new AuthenticationController(authenticationLogic);
        when(authenticationLogic.authenticate(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(true);
        Assertions.assertTrue(authenticationController.authenticate(auth));
        auth = "YWxpY2U6YWw=";
        Assertions.assertFalse(authenticationController.authenticate(auth));
        auth = "Token: YWxpY2VhbA==";
        Assertions.assertFalse(authenticationController.authenticate(auth));
    }

    @Test
    public void authenticateTestNoToken() {
        String auth = "YWxpY2U6YWw=";
        AuthenticationController authenticationController = new AuthenticationController(authenticationLogic);
        when(authenticationLogic.authenticate(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(true);
        Assertions.assertFalse(authenticationController.authenticate(auth));
    }

    @Test
    public void authenticateTestInvalidToken() {
        String auth = "Token: YWxpY2VhbA==";
        AuthenticationController authenticationController = new AuthenticationController(authenticationLogic);
        when(authenticationLogic.authenticate(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(true);
        Assertions.assertFalse(authenticationController.authenticate(auth));
    }
}

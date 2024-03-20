package it.unipd.overture.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.unipd.overture.service.EchoLogic;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.core.EchoMethodCall;
import rs.ltt.jmap.common.method.response.core.EchoMethodResponse;

public class EchoLogicTest {
    @Test
    public void echoTest() {
        String libraryName = "test";
        EchoLogic echoLogic = new EchoLogic();
        EchoMethodCall methodCall = new EchoMethodCall(libraryName);
        MethodResponse[] actualResponse = echoLogic.echo(methodCall);
        String actualLibraryName = ((EchoMethodResponse) actualResponse[0]).getLibraryName();
        Assertions.assertEquals(libraryName, actualLibraryName);
    }
}

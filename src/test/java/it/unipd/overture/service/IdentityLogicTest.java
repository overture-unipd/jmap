package it.unipd.overture.service;

import rs.ltt.jmap.common.entity.Identity;
import it.unipd.overture.port.out.IdentityPort;
import it.unipd.overture.service.IdentityLogic;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.identity.GetIdentityMethodCall;
import rs.ltt.jmap.common.method.response.identity.GetIdentityMethodResponse;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class IdentityLogicTest {
  @Mock IdentityPort identityPort;
  @Mock Identity identity;

  @BeforeEach
  public void setUp() {
      MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getTest() {
    String accountId = "accountId";
    String name = "name";
    String identityInfo = "{\"name\": \"name\"}";
    GetIdentityMethodCall getIdentityMethodCall = new GetIdentityMethodCall(accountId, new String[]{""}, new String[]{""}, null);
    when(identityPort.getOf(accountId)).thenReturn(new Identity[] {identity});
    when(identity.toString()).thenReturn("{\"name\": \"" + name + "\"}");
    IdentityLogic identityLogic = new IdentityLogic(identityPort);
    MethodResponse[] actualResponse = identityLogic.get(getIdentityMethodCall);
    String actualIdentityInfo = ((GetIdentityMethodResponse) actualResponse[0]).getList()[0].toString();
    Assertions.assertTrue(actualResponse[0] instanceof GetIdentityMethodResponse);
    Assertions.assertEquals(identityInfo, actualIdentityInfo);
  } 
}

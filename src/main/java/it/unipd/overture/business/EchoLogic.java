package it.unipd.overture.business;

import com.google.common.collect.ListMultimap;

import rs.ltt.jmap.common.Response;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.core.EchoMethodCall;
import rs.ltt.jmap.common.method.response.core.EchoMethodResponse;

public class EchoLogic {
  public MethodResponse[] echo(EchoMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {
      EchoMethodResponse.builder().libraryName(methodCall.getLibraryName()).build()
    };
  }
}

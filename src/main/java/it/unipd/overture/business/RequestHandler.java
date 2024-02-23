package it.unipd.overture.business;

public class RequestHandler implements RequestPort {
  Dispatcher dispatcher;

  RequestHandler(Dispatcher dispatcher) {
  }

  String session(String json) {
  }

  String postJmap(String id) {
  }

  byte[] download(String id) {
  }

  void upload(Byte[] data) {
  }
}

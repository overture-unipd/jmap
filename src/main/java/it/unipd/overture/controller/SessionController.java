package it.unipd.overture.controller;

import java.util.Base64;

import com.google.gson.Gson;
import com.google.inject.Inject;

import it.unipd.overture.port.in.SessionPort;
import it.unipd.overture.service.SessionLogic;

public class SessionController implements SessionPort {
  private Gson gson;
  private SessionLogic sessionLogic;
  
  @Inject
  SessionController(Gson gson, SessionLogic sessionLogic) {
    this.gson = gson;
    this.sessionLogic = sessionLogic;
  }

  @Override
  public String get(String auth) {
    var token = auth.split(" ");
    var decoded = new String(Base64.getDecoder().decode(token[1]));
    String[] fields = decoded.split(":");
    return gson.toJson(sessionLogic.get(fields[0]));
  }
}

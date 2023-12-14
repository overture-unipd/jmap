package it.unipd.overture.jmap.entities;

import lombok.*;

@Builder
@Getter
public class Account {
  private String address;
  private String password;
}

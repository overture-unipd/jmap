package it.unipd.overture.port.out;

import java.util.List;

public interface ThreadPort {
  List<String> getOf(String accountid, String threadid);
}

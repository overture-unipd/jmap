package it.unipd.overture.business;

public class Identity {
  public get(GetIdentityMethodCall methodCall, ListMultimap<String, Response.Invocation> previousResponses) {
    return new MethodResponse[] {
      GetIdentityMethodResponse.builder()
          .list(
              new Identity[] {
                Identity.builder()
                    .id(accountid)
                    .email(db.getAccountAddress(accountid))
                    .name(db.getAccountName(accountid))
                    .build()
              })
          .build()
    };
  }
}

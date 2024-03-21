# JMAP server - Overture

[![Coverage](.github/badges/jacoco.svg)](https://github.com/overture-unipd/jmap/actions/workflows/build.yml)

Repository contenente il codice del gruppo Overture.

## Tecnologie utilizzate
## Programmi
- [Caddy](https://caddyserver.com/): reverse proxy, per avere il servizio in HTTPS;
- [Docker](https://www.docker.com/): come sistema di containerizzazione;
- [Gradle](https://gradle.org/): sistema di build;
- [Guice](https://github.com/google/guice): framework di dependency injection;
- [Junit](https://junit.org/junit5/): framework Java per unit testing;
- [Locust](https://locust.io/): framework python di load testing;
- [Minio](https://min.io/): per il salvataggio dei file;
- [Mockito](https://site.mockito.org/): framework per mock di oggetti Java;
- [Postman](https://www.postman.com/): programma per il testing delle API;
- [RethinkDB](https://rethinkdb.com/): come database;
- [Spark](https://sparkjava.com/): framework minimale per esposizione endpoint HTTP;
- [TestContainers](https://testcontainers.com/): framework per integration testing;
- [iNPUTmice jmap](https://codeberg.org/iNPUTmice/jmap/): libreria Java contenente gli oggetti di dominio;

## Esecuzione del server
```
just build # build the container
just up
```

Il server sta ora girando in un container, con bind sulla porta 443.
Per resettare il database allo stato iniziale:
```
just reset
```

Per fermare il servizio:
```
just down
```

Per distruggere container e volumi:
```
just destroy
```

## Collegamento con il client Android `Ltt.rs`

Il client android [Ltt.rs](https://codeberg.org/iNPUTmice/lttrs-android) richiede che il server utilizzi HTTPS con un FQDN.

Utilizziamo [DuckDNS](https://www.duckdns.org/) per avere un dominio gratuito e [Caddy](https://caddyserver.com/) che funge da reverse proxy con HTTPS.

Per dettagli fare riferimento al manuale utente nella [repository della documentazione](https://github.com/overture-unipd/jmap/tree/develop).

## Setup per sviluppo locale (senza build immagine Docker)
Dopo aver abilitato le porte del database (togliendo il commento in `compose.yml`):
```
just up database
just run
```

Il server gira sulla porta 8000.

Eventualmente, per loggare il traffico con wireshark:
```
just wireshark
```

Per eseguire i test di unità ed integrazione impostati:
```
just test
```

## Setup dei controlli con `pre-commit`
Installa `pre-commit` ed imposta l'hook per Git.
```
pip install pre-commit
pre-commit install
```

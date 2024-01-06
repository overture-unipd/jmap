# JMAP server - Overture

Repository contenente il codice del gruppo Overture.

## Tecnologie usate
- [Just](https://github.com/casey/just): per l'esecuzione dei comandi frequenti;
- RethinkDB: come database;
- Gradle: sistema di build;
- Docker: come sistema di containerizzazione;
- EditorConfig: impostazioni di formattazione comuni;
- pre-commit: hook per git, controlla e sistema la formattazione del codice;
- Caddy: reverse proxy, per avere il servizio in HTTPS.

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

## Setup per sviluppo locale (senza build immagine Docker)
Dopo aver abilitato le porte del database (togliendo il commento in `compose.yml`):
```
just up database
just run
```

Il server gira sulla porta 8000.

Eventualmente, per loggare il trafficon con wireshark:
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

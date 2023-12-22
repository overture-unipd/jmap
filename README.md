# JMAP server - Overture

Repository contenente il codice del gruppo Overture.

Usiamo [Just](https://github.com/casey/just) per l'esecuzione dei comandi.

## Esecuzione del server
```
just build # build the container
just up
```

Il server sta ora girando in un container, con bind sulla porta 8000.
Per resettare il database allo stato iniziale:
```
just reset-db
```

Per distruggere i container
```
just down
```

Per distruggere tutto (container orfani e volumi)
```
just destroy
```

## Sviluppo senza immagine Docker per il server
Dopo aver abilitato le porte del database (togliendo il commento in `compose.yml`):
```
just up database
just run
```

Il server gira sulla porta 8000.

## Controlli con `pre-commit`
Installa `pre-commit` ed imposta l'hook per Git.
```
pip install pre-commit
pre-commit install
```

## Connettere un client
```
git clone https://github.com/linagora/tmail-flutter
cd tmail-flutter
echo "SERVER_URL=http://localhost:8000" > env.file
docker build -t tmail-web:latest .
docker run -ti -p 9000:80 --name tmail tmail-web
```

Aprire con un browser: http://localhost:9000
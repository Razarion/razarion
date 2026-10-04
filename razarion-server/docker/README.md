cd C:\dev\projects\razarion\code\razarion\razarion-server\src\test\resources\docker

docker-compose up -d
docker-compose up --build --force-recreate -d

docker-compose down
docker volume prune

## Two checkouts on one machine

Each checkout can run its own stack, so two working copies (e.g. two Claude sessions) do not
write into the same database. The second checkout puts a `.env` next to `docker-compose.yml`
(git-ignored):

```
COMPOSE_PROJECT_NAME=razarion2
RAZ_DB_CONTAINER=db2
RAZ_DB_PORT=32789
RAZ_MONGO_PORT=27018
RAZ_MAILHOG_CONTAINER=mailhog2
RAZ_SMTP_PORT=1026
RAZ_MAILHOG_UI_PORT=8026
RAZ_SERVER_PORT=8081
```

- `docker compose up -d` then starts `db2`, `razarion2-mongo-1` and `mailhog2` on those ports.
  `COMPOSE_PROJECT_NAME` matters: without it both checkouts are project `docker` and one would
  recreate the other's containers.
- The server (profile `local`) reads the same file (`spring.config.import` in
  `application-local.properties`) and starts on `RAZ_SERVER_PORT` against those databases.
- The frontend proxy (`razarion-frontend/src/proxy.conf.js`) targets `RAZ_SERVER_PORT` as well;
  start the dev server on a free port: `npm start -- --port 4201`.

Copy the data over once from the first stack:

```
docker exec db mariadb-dump -uroot -p1234 --single-transaction --max_allowed_packet=512M razarion | docker exec -i db2 mariadb -uroot -p1234 --max_allowed_packet=512M razarion
docker exec docker-mongo-1 mongodump --db razarion --archive | docker exec -i razarion2-mongo-1 mongorestore --archive --drop
```

Without a `.env` everything runs on the default ports, as before.

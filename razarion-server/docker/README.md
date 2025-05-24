cd C:\dev\projects\razarion\code\razarion\razarion-server\src\test\resources\docker

docker-compose up -d
docker-compose up --build --force-recreate -d

docker-compose down
docker volume prune

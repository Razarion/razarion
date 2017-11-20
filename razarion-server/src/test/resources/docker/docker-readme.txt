# !!! On windows line separator must be corrected on shell scripts (LF UNIX)
# Start Docker Quick Start Terminal if Dockertools is used
# cd /c/dev/projects/razarion/code/razarion/razarion-server/src/test/resources/docker

# Build container
docker build -t wildfly_ok_1 .

# Run container
docker run --name wildfly_ok_1_instance --rm -p 32778:8080 -p 32768:9990 -p 32771:3306 -p 32769:8787 -p 32777:27017 -t wildfly_ok_1

# Show only running containers
docker ps

# Kill one or more running containers
docker kill wildfly_ok_1_instance

# Remove container
docker rm wildfly_ok_1_instance

# Attach local standard input, output, and error streams to a running container
docker attach  wildfly_ok_1_instance

# Remove one or more images
docker image rm wildfly_ok_1

# Remove all
docker rm -f $(docker ps -a -q)
docker rmi -f $(docker images -q)
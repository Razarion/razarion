FROM jboss/wildfly:10.0.0.Final

USER root
COPY container-files/mariadb/etc/yum.repos.d/* /etc/yum.repos.d/

RUN \
    yum update -y && \
    yum install -y epel-release MariaDB-server hostname net-tools pwgen wget && \
    yum clean all && \
    rm -rf /var/lib/mysql/*

RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/mariadb/mariadb-java-client/main
RUN wget http://central.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/1.3.7/mariadb-java-client-1.3.7.jar -O /opt/jboss/wildfly/modules/system/layers/base/org/mariadb/mariadb-java-client/main/mariadb-java-client-1.3.7.jar

COPY container-files/wildfly/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml
COPY container-files/wildfly/module.xml /opt/jboss/wildfly/modules/system/layers/base/org/mariadb/mariadb-java-client/main/

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#007 --silent

COPY container-files/mariadb /
COPY container-files/wildfly/run-wildfly.sh /

EXPOSE 3306 8080 9990 8787
WORKDIR "/"
ENV MARIADB_PASS 'admin'
RUN chmod +x /run-maria.sh
RUN chmod +x /run-wildfly.sh
ENTRYPOINT ./run-maria.sh && ./run-wildfly.sh

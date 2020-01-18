FROM jboss/wildfly:18.0.1.Final

ADD standalone-custom.xml /opt/jboss/wildfly/standalone/configuration

COPY jdbc-driver /opt/jboss/wildfly/modules/system/layers/base

RUN /opt/jboss/wildfly/bin/add-user.sh admin 1234 --silent

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-custom.xml","-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

java.lang.RuntimeException: java.lang.ClassCastException: com.sun.el.ExpressionFactoryImpl cannot be cast to javax.el.ExpressionFactory
- https://issues.liferay.com/browse/LPS-67659
- https://issues.liferay.com/browse/LPS-65488
- https://issues.liferay.com/browse/LPS-67036
Fix:
put portal-ext.properties in: C:\dev\tech\liferay\liferay-ce-portal-7.0-ga3\wildfly-10.0.0\standalone\deployments\ROOT.war\WEB-INF\classes\
portal-ext.properties content:
module.framework.properties.org.osgi.framework.bootdelegation=\
    __redirected,\
    com.sun.ccpp,\
    com.sun.ccpp.*,\
    com.sun.crypto.*,\
    com.sun.image.*,\
    com.sun.jmx.*,\
    com.sun.jna,\
    com.sun.jndi.*,\
    com.sun.mail.*,\
    com.sun.management.*,\
    com.sun.media.*,\
    com.sun.msv.*,\
    com.sun.org.*,\
    com.sun.syndication,\
    com.sun.tools.*,\
    com.sun.xml.*,\
    com.yourkit.*,\
    sun.*
---------------------------------------------------------------------------------------------------------------------------------
Increase heap memory for wildfly
bin/standalone.conf
---------------------------------------------------------------------------------------------------------------------------------
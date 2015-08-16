package com.btxtech.server;

import com.btxtech.shared.FieldVerifier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.logging.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet {
     private Logger logger = Logger.getLogger(GreetingServiceImpl.class.getName());
    private final org.slf4j.Logger log = getLogger(GreetingServiceImpl.class);

    public GreetingServiceImpl() {
        System.out.println("------ GreetingServiceImpl -------------");
        logger.finest("--- finest");
        logger.finer("--- finer");
        logger.fine("--- fine");
        logger.info("--- info");
        logger.warning("--- warning");
        logger.severe("--- severe");

        log.debug("++++ debug");
        log.info("++++ info");
        log.warn("++++ warn");
        log.error("++++ error");
    }

    public String greetServer(String input) throws IllegalArgumentException {
    // Verify that the input is valid.
    if (!FieldVerifier.isValidName(input)) {
      // If the input is not valid, throw an IllegalArgumentException back to
      // the client.
      throw new IllegalArgumentException(
          "Name must be at least 4 characters long");
    }

    String serverInfo = getServletContext().getServerInfo();
    String userAgent = getThreadLocalRequest().getHeader("User-Agent");

    // Escape data from the client to avoid cross-site script vulnerabilities.
    input = escapeHtml(input);
    userAgent = escapeHtml(userAgent);

    return "Hello, " + input + "!<br><br>I am running " + serverInfo
        + ".<br><br>It looks like you are using:<br>" + userAgent;
  }

  /**
   * Escape an html string. Escaping data received from the client helps to
   * prevent cross-site script vulnerabilities.
   *
   * @param html the html string to escape
   * @return the escaped string
   */
  private String escapeHtml(String html) {
    if (html == null) {
      return null;
    }
    return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(
        ">", "&gt;");
  }
}

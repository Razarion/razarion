package application;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 18.02.2017.
 */
// TODO does not work. Is not called
// @Path(RestUrl.REMOTE_LOGGING)
@Path("remote_logging")
public class LoggingProvider {
    // private Logger logger = Logger.getLogger(LoggingProvider.class.getName());
    private Log log = LogFactoryUtil.getLog(LoggingProvider.class);

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("simple")
    public void simpleLogger(String logString) {
        // logger.severe("SimpleLogger: " + logString);
        log.error("LoggingProvider.simpleLogger: " + logString);
    }

    @GET
    @Path("/morning")
    @Produces("text/plain")
    public String hello() {
        return "Good morning!";
    }

}

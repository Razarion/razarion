package com.btxtech.facebookmarketing;

import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 17.03.2017.
 */
public class FacebookAdMain {
    private static final String ACCESS_TOKEN = "";
    // private static final String SECRET = "";
    // private static final String MARKETING_ACCOUNT = "";

    public static void main(String[] args) {
        try {
            //APIContext apiContext = new APIContext(ACCESS_TOKEN, SECRET).enableDebug(true);
            //AdAccount adAccount = new AdAccount(MARKETING_ACCOUNT, apiContext);
            Client client = ClientBuilder.newClient();
            client.register(new LoggingFilter());
            long addId = 6065802095221L;
            String fields = "{\"access_token\": \"" + ACCESS_TOKEN + "\", \"url\": \"https://www.razarion.com\", \"add_template_param\": \"1\"}";

            Object returnValue = client.target("https://graph.facebook.com/v2.8").path(Long.toString(addId)).path("trackingtag").request(MediaType.APPLICATION_JSON).post(Entity.entity(fields, MediaType.APPLICATION_JSON_TYPE), String.class);
            System.out.println(returnValue);
            System.out.println("----------------------------");
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

}

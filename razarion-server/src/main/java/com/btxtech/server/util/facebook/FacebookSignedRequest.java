package com.btxtech.server.util.facebook;

import java.io.Serializable;

/**
 * User: beat
 * Date: 22.07.12
 * Time: 14:44
 */
public class FacebookSignedRequest implements Serializable {
    private String algorithm;
    private long issued_at; // camel notation does not work here
    private FacebookUser user;
    private String oauth_token; // camel notation does not work here
    private String user_id; // camel notation does not work here
    private String email;
    private String firstName;
    private String lastName;
    private String link;

    public FacebookSignedRequest(String algorithm, long issuedAt, FacebookUser user, String oAuthToken, String userId) {
        this.algorithm = algorithm;
        issued_at = issuedAt;
        this.user = user;
        oauth_token = oAuthToken;
        user_id = userId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public long getIssuedAt() {
        return issued_at;
    }

    public FacebookUser getUser() {
        return user;
    }

    public String getOAuthToken() {
        return oauth_token;
    }

    public boolean hasOAuthToken() {
        return oauth_token != null && !oauth_token.isEmpty();
    }

    public String getUserId() {
        return user_id;
    }

    public boolean hasUserId() {
        return user_id != null && !user_id.isEmpty();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String secondName) {
        this.lastName = secondName;
    }
}

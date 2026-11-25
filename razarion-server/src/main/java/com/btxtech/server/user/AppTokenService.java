package com.btxtech.server.user;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * The login token of a password account - what the browser keeps as {@code app.token} and sends
 * with every request. Issued after a password login ({@code /rest/user/auth}) and after a sign-in
 * link by mail ({@link ProgressSaveService#useLink}).
 */
@Service
public class AppTokenService {
    static final long VALID_SECONDS = 36000L;
    private final JwtEncoder encoder;

    public AppTokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * @param email the account's login, the token's subject
     * @param scope the roles, separated by blanks; empty for a player
     */
    public String issue(String email, String scope) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(VALID_SECONDS))
                .subject(email)
                .claim("scope", scope)
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

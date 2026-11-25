package com.btxtech.server.rest;

import com.btxtech.server.model.RegisterRequest;
import com.btxtech.server.model.RegisterResult;
import com.btxtech.server.model.SetNameResult;
import com.btxtech.server.user.AppTokenService;
import com.btxtech.server.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/user")
public class UserController {
    private final AppTokenService appTokenService;
    private final UserService userService;

    public UserController(AppTokenService appTokenService, UserService userService) {
        this.appTokenService = appTokenService;
        this.userService = userService;
    }

    @PostMapping("auth")
    public String auth(Authentication authentication) {
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        return appTokenService.issue(authentication.getName(), scope);
    }

    @GetMapping("checkToken")
    public void checkToken() {
    }

    @PostMapping("registerByEmail")
    public RegisterResult registerByEmail(@RequestBody RegisterRequest registerRequest) {
        return userService.registerByEmail(registerRequest.getEmail(), registerRequest.getPassword());
    }

    @PostMapping("verifyEmailVerificationId")
    public boolean verifyEmailVerificationId(@RequestBody String verificationId) {
        return this.userService.verifyEmailVerificationId(verificationId);
    }

    @DeleteMapping("deleteUser")
    public void deleteUser() {
        this.userService.deleteUser();
    }

    @PostMapping(value = "setname/{name}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    SetNameResult setName(@PathVariable("name") String name) {
        return this.userService.setName(name);
    }

    @GetMapping(value = "verifySetName/{name}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    SetNameResult verifySetName(@PathVariable("name") String name) {
        return new SetNameResult().errorResult(this.userService.verifySetName(name));
    }

}

package com.limbrescue.limbrescueangularappbackend.controller;
import com.limbrescue.limbrescueangularappbackend.model.AuthenticationBean;
import org.springframework.web.bind.annotation.*;

//Port number 8081
@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class BasicAuthController {
    /**
     * Authorizes the login.
     *
     * @return
     *          The authentication bean.
     */
    //@GetMapping(path = "/basicauth")
    @PostMapping("/signin")
    public AuthenticationBean helloWorldBean() {
        return new AuthenticationBean("You are authenticated");
    }
}

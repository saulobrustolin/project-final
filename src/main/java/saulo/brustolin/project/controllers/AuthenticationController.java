package saulo.brustolin.project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.auth.AuthenticationDTO;
import saulo.brustolin.project.dtos.auth.RegisterDTO;
import saulo.brustolin.project.services.AuthenticationService;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> login(
        AuthenticationDTO dto,
        HttpServletResponse response
    ) {
        authenticationService.authenticate(dto, response);

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> register(
        RegisterDTO dto,
        HttpServletResponse response
    ) {
        authenticationService.register(dto, response);

        return ResponseEntity.ok().build();
    }
}

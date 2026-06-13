package saulo.brustolin.project.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.auth.AuthenticationDTO;
import saulo.brustolin.project.dtos.auth.LoginResponseDTO;
import saulo.brustolin.project.services.AuthenticationService;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    public LoginResponseDTO login(AuthenticationDTO dto) {
        return authenticationService.authenticate(dto);
    }
}

package saulo.brustolin.project.controllers;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.users.ResumeUserDTO;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.services.UserService;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    
    public final UserService userService;

    @GetMapping(path = "/resume", produces = "application/json")
    public ResumeUserDTO getResume(
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth period
    ) {
        return userService.getResume(user, period);
    }
}

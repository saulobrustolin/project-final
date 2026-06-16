package saulo.brustolin.project.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.users.MeDTO;
import saulo.brustolin.project.dtos.users.ResumeUserDTO;
import saulo.brustolin.project.dtos.users.UpdateUserDTO;
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
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to
    ) {
        return userService.getResume(user, from, to);
    }

    @GetMapping(path = "/me", produces = "application/json")
    public MeDTO getMe(@AuthenticationPrincipal User user) {
        return new MeDTO(
            user.getName(),
            user.getEmail(),
            user.getBalance()
        );
    }

    @PatchMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> update(@AuthenticationPrincipal User user, @RequestBody @Valid UpdateUserDTO dto) {
        userService.update(user, dto);

        return ResponseEntity.ok().build();
    }
}

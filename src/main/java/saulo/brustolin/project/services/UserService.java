package saulo.brustolin.project.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.repositories.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDetails loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}

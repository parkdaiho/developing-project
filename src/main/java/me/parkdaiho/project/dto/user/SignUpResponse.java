package me.parkdaiho.project.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
@AllArgsConstructor
public class SignUpResponse {

    private String username;
    private String password;
}

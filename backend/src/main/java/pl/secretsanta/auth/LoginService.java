package pl.secretsanta.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;
import pl.secretsanta.auth.dto.LoginRequest;
import pl.secretsanta.auth.dto.LoginResponse;
import pl.secretsanta.auth.dto.UserResponse;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class LoginService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService tokens;
    private final String dummyHash;

    public LoginService(AppUserRepository users, PasswordEncoder passwordEncoder, AccessTokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        // An unknown email still goes through BCrypt to reduce timing differences.
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public LoginResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Invalid credentials");
        }
        AppUser user = users.findByEmail(request.email()).orElse(null);
        String hash = user == null ? dummyHash : user.getPasswordHash();
        if (!passwordEncoder.matches(request.password(), hash) || user == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return new LoginResponse(tokens.issue(user.getId()), "Bearer", AccessTokenService.TTL.toSeconds(),
                new UserResponse(user.getId(), user.getEmail()));
    }

    public UserResponse currentUser(UUID id) {
        return users.findById(id)
                .map(user -> new UserResponse(user.getId(), user.getEmail()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}

package pl.secretsanta.auth;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        AppUser user = new AppUser(request.email(), passwordHash, Instant.now().truncatedTo(ChronoUnit.MICROS));
        try {
            // Send the INSERT now so a concurrent duplicate is detected inside this method.
            users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateEmail(exception)) {
                throw new EmailAlreadyRegisteredException();
            }
            throw exception;
        }
        return new RegisterResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }

    private boolean isDuplicateEmail(DataIntegrityViolationException exception) {
        for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                return "23505".equals(violation.getSQLState())
                        && "uk_app_users_email".equals(violation.getConstraintName());
            }
        }
        return false;
    }
}

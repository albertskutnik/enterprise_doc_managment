package pl.askutnik.edm.users;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController 
@RequestMapping("/api/auth") 
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new IllegalArgumentException("Wrong email or password"));

        if (!user.getPassword().equals(request.password())) {
            throw new IllegalArgumentException("Wrong email or password");
        }

        return new LoginResponse(
            user.getId(),
            user.getEmail(),
            user.getRole()
        );
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    public record LoginRequest(
        String email,
        String password
    ) {
    }

    public record LoginResponse(
        UUID userId,
        String email,
        UserRole role
    ) {
    }

    public record ErrorResponse(String message) {
    }

}

package pl.askutnik.edm.users;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = User.create(
            request.email(),
            request.password(),
            request.role()
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll()
            .stream()
            .map(UserResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return UserResponse.from(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userRepository.deleteById(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    public record CreateUserRequest(
        String email,
        String password,
        UserRole role
    ) {
    }

    public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        Instant createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
            );
        }
    }

    public record ErrorResponse(String message) {
    }
}

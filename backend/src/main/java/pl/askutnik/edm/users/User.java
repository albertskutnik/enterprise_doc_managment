package pl.askutnik.edm.users;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    private UUID id;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private Instant createdAt;

    protected User() {
    }

    private User(UUID id, String email, String password, UserRole role, Instant createdAt) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User create(String email, String password, UserRole role) {
        return new User(
            UUID.randomUUID(),
            email,
            password,
            role,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

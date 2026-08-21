package pl.askutnik.edm.users;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    private UUID id;

    private String username;
    private String role;
    private Instant createdAt;

    protected User() {
    }

    private User(UUID id, String username, String role, Instant createdAt) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }

        this.id = id;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User create(String username, String role) {
        return new User(
            UUID.randomUUID(),
            username,
            role,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

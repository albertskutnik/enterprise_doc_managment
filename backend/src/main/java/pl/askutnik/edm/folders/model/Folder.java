package pl.askutnik.edm.folders.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    private UUID id;

    private String name;
    private Instant createdAt;

    protected Folder() {
    }

    private Folder(UUID id, String name, Instant createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("Folder id cannot be empty");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Folder name cannot be empty");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Folder creation date cannot be empty");
        }

        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static Folder create(String name) {
        return new Folder(
            UUID.randomUUID(),
            name,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

package pl.askutnik.edm.sharing;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "share_links")
public class ShareLink {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    private UUID documentId;
    private Instant expiresAt;
    private Instant createdAt;

    protected ShareLink() {
    }

    private ShareLink(
        UUID id,
        String token,
        UUID documentId,
        Instant expiresAt,
        Instant createdAt
    ) {
        this.id = id;
        this.token = token;
        this.documentId = documentId;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static ShareLink create(UUID documentId, Instant expiresAt) {
        return new ShareLink(
            UUID.randomUUID(),
            UUID.randomUUID().toString(),
            documentId,
            expiresAt,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

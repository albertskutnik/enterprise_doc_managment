package pl.askutnik.edm.documents;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_accesses")
public class DocumentAccess {

    @Id 
    private UUID id;

    private UUID documentId;
    private UUID userId;
    private String accessType;
    private Instant createdAt;

    protected DocumentAccess() {
    }

    private DocumentAccess(UUID id, UUID documentId, UUID userId, String accessType, Instant createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.userId = userId;
        this.accessType = accessType;
        this.createdAt = createdAt;
    }
    
    
    public static DocumentAccess create(UUID documentId, UUID userId, String accessType) {
        return new DocumentAccess(UUID.randomUUID(), documentId, userId, accessType, Instant.now());
    }


    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAccessType() {
        return accessType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}

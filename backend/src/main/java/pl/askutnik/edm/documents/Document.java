package pl.askutnik.edm.documents;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    private UUID id;

    private String name;
    private String contentType;
    private long size;
    private String storageFileName;
    private UUID folderId;
    private Instant createdAt;

    protected Document() {
    }

    private Document(
        UUID id,
        String name,
        String contentType,
        long size,
        String storageFileName,
        UUID folderId,
        Instant createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Document id cannot be empty");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Document name cannot be empty");
        }

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Document content type cannot be empty");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Document size must be greater than zero");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Document creation date cannot be empty");
        }

        if (storageFileName == null || storageFileName.isBlank()) {
            throw new IllegalArgumentException("Document storage file name cannot be empty");
        }

        this.id = id;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.storageFileName = storageFileName;
        this.folderId = folderId;
        this.createdAt = createdAt;
    }

    public static Document create(
        String name,
        String contentType,
        long size,
        String storageFileName,
        UUID folderId
    ) {
        return new Document(
            UUID.randomUUID(),
            name,
            contentType,
            size,
            storageFileName,
            folderId,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getStorageFileName() {
        return storageFileName;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

package pl.askutnik.edm.documents.model;

import java.time.Instant;
import java.util.UUID;

public record Document(
    UUID id,
    String name,
    String contentType,
    long size,
    String storageFileName,
    Instant createdAt
) {
    public Document {
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
    }

    public static Document create(String name, String contentType, long size, String storageFileName) {
        return new Document(
            UUID.randomUUID(),
            name,
            contentType,
            size,
            storageFileName,
            Instant.now()
        );
    }
}

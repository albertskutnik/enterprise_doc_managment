package pl.askutnik.edm.documents.infrastructure.in.web;

import java.time.Instant;
import java.util.UUID;

import pl.askutnik.edm.documents.model.Document;

public class DocumentResponse {
    private UUID id;
    private String name;
    private String contentType;
    private long size;
    private Instant createdAt;

    public DocumentResponse(UUID id, String name, String contentType, long size, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
    }

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
            document.id(),
            document.name(),
            document.contentType(),
            document.size(),
            document.createdAt()
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
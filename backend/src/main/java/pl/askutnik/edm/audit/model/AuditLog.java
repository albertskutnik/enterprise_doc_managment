package pl.askutnik.edm.audit.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private UUID id;

    private String eventType;
    private String resourceType;
    private UUID resourceId;
    private boolean success;
    private String message;
    private Instant createdAt;

    protected AuditLog() {
    }

    private AuditLog(
        UUID id,
        String eventType,
        String resourceType,
        UUID resourceId,
        boolean success,
        String message,
        Instant createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Audit log id cannot be empty");
        }

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Audit log event type cannot be empty");
        }

        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Audit log resource type cannot be empty");
        }

        if (resourceId == null) {
            throw new IllegalArgumentException("Audit log resource id cannot be empty");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Audit log creation date cannot be empty");
        }

        this.id = id;
        this.eventType = eventType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.success = success;
        this.message = message;
        this.createdAt = createdAt;
    }

    public static AuditLog create(
        String eventType,
        String resourceType,
        UUID resourceId,
        boolean success,
        String message
    ) {
        return new AuditLog(
            UUID.randomUUID(),
            eventType,
            resourceType,
            resourceId,
            success,
            message,
            Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

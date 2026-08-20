package pl.askutnik.edm.audit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public List<AuditLogResponse> listAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(AuditLogResponse::from)
            .toList();
    }

    public record AuditLogResponse(
        UUID id,
        String eventType,
        String resourceType,
        UUID resourceId,
        boolean success,
        String message,
        Instant createdAt
    ) {
        public static AuditLogResponse from(AuditLog auditLog) {
            return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getEventType(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.isSuccess(),
                auditLog.getMessage(),
                auditLog.getCreatedAt()
            );
        }
    }
}

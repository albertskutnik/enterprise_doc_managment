package pl.askutnik.edm.audit.infrastructure.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import pl.askutnik.edm.audit.infrastructure.in.web.AuditLogController.AuditLogResponse;
import pl.askutnik.edm.audit.infrastructure.out.persistence.AuditLogRepository;
import pl.askutnik.edm.audit.model.AuditLog;

class AuditLogControllerTest {

    @Test
    void shouldListAuditLogs() {
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        AuditLog auditLog = AuditLog.create(
            "DOCUMENT_CREATED",
            "DOCUMENT",
            UUID.randomUUID(),
            true,
            "Document was created"
        );

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(auditLog));

        AuditLogController auditLogController = new AuditLogController(auditLogRepository);

        List<AuditLogResponse> response = auditLogController.listAuditLogs();

        assertEquals(1, response.size());
        assertEquals("DOCUMENT_CREATED", response.get(0).eventType());
        assertEquals("DOCUMENT", response.get(0).resourceType());
        assertEquals(true, response.get(0).success());
        assertEquals("Document was created", response.get(0).message());
    }
}

package pl.askutnik.edm.sharing.infrastructure.in.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.infrastructure.out.persistence.AuditLogRepository;
import pl.askutnik.edm.audit.model.AuditLog;
import pl.askutnik.edm.documents.infrastructure.out.persistence.DocumentRepository;
import pl.askutnik.edm.documents.model.Document;
import pl.askutnik.edm.sharing.infrastructure.in.web.ShareLinkController.CreateShareLinkRequest;
import pl.askutnik.edm.sharing.infrastructure.in.web.ShareLinkController.ShareLinkResponse;
import pl.askutnik.edm.sharing.infrastructure.out.persistence.ShareLinkRepository;
import pl.askutnik.edm.sharing.model.ShareLink;

class ShareLinkControllerTest {

    private List<Document> documents;
    private List<ShareLink> shareLinks;
    private List<AuditLog> auditLogs;
    private ShareLinkController shareLinkController;

    @TempDir
    private Path uploadDirectory;

    @BeforeEach
    void setUp() {
        documents = new ArrayList<>();
        shareLinks = new ArrayList<>();
        auditLogs = new ArrayList<>();
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        ShareLinkRepository shareLinkRepository = mock(ShareLinkRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);

        when(documentRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> document.getId().equals(id))
                .findFirst();
        });

        when(shareLinkRepository.save(any(ShareLink.class))).thenAnswer(invocation -> {
            ShareLink shareLink = invocation.getArgument(0);
            shareLinks.add(shareLink);
            return shareLink;
        });

        when(shareLinkRepository.findByToken(any(String.class))).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);

            return shareLinks.stream()
                .filter(shareLink -> shareLink.getToken().equals(token))
                .findFirst();
        });

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            auditLogs.add(auditLog);
            return auditLog;
        });

        shareLinkController = new ShareLinkController(
            documentRepository,
            shareLinkRepository,
            auditLogRepository,
            uploadDirectory.toString()
        );
    }

    @Test
    void shouldCreateShareLink() {
        Document document = Document.create(
            "test.txt",
            "text/plain",
            5,
            "stored-test.txt",
            null
        );
        documents.add(document);

        ShareLinkResponse response = shareLinkController.createShareLink(
            document.getId(),
            new CreateShareLinkRequest(24)
        );

        assertNotNull(response.id());
        assertNotNull(response.token());
        assertEquals(document.getId(), response.documentId());
        assertNotNull(response.expiresAt());
        assertNotNull(response.createdAt());
        assertEquals("/api/share-links/" + response.token() + "/download", response.downloadPath());
        assertEquals(1, shareLinks.size());
        assertEquals(1, auditLogs.size());
        assertEquals("SHARE_LINK_CREATED", auditLogs.get(0).getEventType());
    }

    @Test
    void shouldReturnNotFoundWhenDocumentDoesNotExistDuringShareLinkCreation() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> shareLinkController.createShareLink(
                UUID.randomUUID(),
                new CreateShareLinkRequest(24)
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldDownloadDocumentByShareLink() throws IOException {
        byte[] content = "hello".getBytes();
        String storageFileName = "stored-test.txt";
        Files.write(uploadDirectory.resolve(storageFileName), content);
        Document document = Document.create(
            "test.txt",
            "text/plain",
            content.length,
            storageFileName,
            null
        );
        ShareLink shareLink = ShareLink.create(
            document.getId(),
            Instant.now().plusSeconds(3600)
        );
        documents.add(document);
        shareLinks.add(shareLink);

        ResponseEntity<Resource> response = shareLinkController.downloadDocumentByShareLink(shareLink.getToken());
        Resource resource = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("text/plain", response.getHeaders().getContentType().toString());
        assertNotNull(resource);
        assertArrayEquals(content, resource.getInputStream().readAllBytes());
        assertEquals(1, auditLogs.size());
        assertEquals("SHARE_LINK_USED", auditLogs.get(0).getEventType());
    }

    @Test
    void shouldReturnNotFoundWhenTokenDoesNotExist() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> shareLinkController.downloadDocumentByShareLink("missing-token")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldReturnGoneWhenShareLinkIsExpired() {
        ShareLink shareLink = ShareLink.create(
            UUID.randomUUID(),
            Instant.now().minusSeconds(3600)
        );
        shareLinks.add(shareLink);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> shareLinkController.downloadDocumentByShareLink(shareLink.getToken())
        );

        assertEquals(HttpStatus.GONE, exception.getStatusCode());
        assertEquals(1, auditLogs.size());
        assertEquals("SHARE_LINK_EXPIRED", auditLogs.get(0).getEventType());
    }
}

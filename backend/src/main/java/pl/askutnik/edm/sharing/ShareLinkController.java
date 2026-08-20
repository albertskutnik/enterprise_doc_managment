package pl.askutnik.edm.sharing;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.AuditLog;
import pl.askutnik.edm.audit.AuditLogRepository;
import pl.askutnik.edm.documents.Document;
import pl.askutnik.edm.documents.DocumentRepository;

@RestController
@RequestMapping("/api")
public class ShareLinkController {

    private final Path uploadDirectory;
    private final DocumentRepository documentRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final AuditLogRepository auditLogRepository;

    public ShareLinkController(
        DocumentRepository documentRepository,
        ShareLinkRepository shareLinkRepository,
        AuditLogRepository auditLogRepository,
        @Value("${app.upload-dir}") String uploadDirectory
    ) {
        this.documentRepository = documentRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.auditLogRepository = auditLogRepository;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostMapping("/documents/{documentId}/share-links")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareLinkResponse createShareLink(
        @PathVariable UUID documentId,
        @RequestBody CreateShareLinkRequest request
    ) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ShareLink shareLink = ShareLink.create(
            document.getId(),
            Instant.now().plusSeconds(request.expiresInHours() * 60 * 60)
        );
        ShareLink savedShareLink = shareLinkRepository.save(shareLink);
        auditLogRepository.save(AuditLog.create(
            "SHARE_LINK_CREATED",
            "SHARE_LINK",
            savedShareLink.getId(),
            true,
            "Share link was created"
        ));

        return ShareLinkResponse.from(savedShareLink);
    }

    @GetMapping("/share-links/{token}/download")
    public ResponseEntity<Resource> downloadDocumentByShareLink(
        @PathVariable String token
    ) throws MalformedURLException {
        ShareLink shareLink = shareLinkRepository.findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (shareLink.getExpiresAt().isBefore(Instant.now())) {
            auditLogRepository.save(AuditLog.create(
                "SHARE_LINK_EXPIRED",
                "SHARE_LINK",
                shareLink.getId(),
                true,
                "Share link is expired"
            ));
            throw new ResponseStatusException(HttpStatus.GONE);
        }

        Document document = documentRepository.findById(shareLink.getDocumentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Path filePath = uploadDirectory.resolve(document.getStorageFileName());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        auditLogRepository.save(AuditLog.create(
            "SHARE_LINK_USED",
            "SHARE_LINK",
            shareLink.getId(),
            true,
            "Share link was used"
        ));

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.getContentType()))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(document.getName())
                    .build()
                    .toString()
            )
            .body(resource);
    }

    public record CreateShareLinkRequest(long expiresInHours) {
    }

    public record ShareLinkResponse(
        UUID id,
        String token,
        UUID documentId,
        Instant expiresAt,
        Instant createdAt,
        String downloadPath
    ) {
        public static ShareLinkResponse from(ShareLink shareLink) {
            return new ShareLinkResponse(
                shareLink.getId(),
                shareLink.getToken(),
                shareLink.getDocumentId(),
                shareLink.getExpiresAt(),
                shareLink.getCreatedAt(),
                "/api/share-links/" + shareLink.getToken() + "/download"
            );
        }
    }
}

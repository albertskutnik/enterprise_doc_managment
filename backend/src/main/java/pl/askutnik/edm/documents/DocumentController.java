package pl.askutnik.edm.documents;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.AuditLog;
import pl.askutnik.edm.audit.AuditLogRepository;
import pl.askutnik.edm.folders.FolderRepository;
import pl.askutnik.edm.users.User;
import pl.askutnik.edm.users.UserRepository;
import pl.askutnik.edm.users.UserRole;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final Path uploadDirectory;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;
    private final FolderRepository folderRepository;
    private final DocumentAccessRepository documentAccessRepository;
    private final UserRepository userRepository;

    public DocumentController(
        DocumentRepository documentRepository,
        AuditLogRepository auditLogRepository,
        FolderRepository folderRepository,
        DocumentAccessRepository documentAccessRepository,
        UserRepository userRepository,
        @Value("${app.upload-dir}") String uploadDirectory
    ) throws IOException {
        this.documentRepository = documentRepository;
        this.auditLogRepository = auditLogRepository;
        this.folderRepository = folderRepository;
        this.documentAccessRepository = documentAccessRepository;
        this.userRepository = userRepository;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDirectory);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("ownerId") UUID ownerId,
        @RequestParam(name = "folderId", required = false) UUID folderId
    ) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (folderId != null && !folderRepository.existsById(folderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!userRepository.existsById(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String storageFileName = UUID.randomUUID() + "-" + originalFileName;
        Path storagePath = uploadDirectory.resolve(storageFileName);

        file.transferTo(storagePath);

        Document document = Document.create(
            originalFileName,
            file.getContentType(),
            file.getSize(),
            storageFileName,
            folderId,
            ownerId
        );

        Document savedDocument = documentRepository.save(document);
        saveAuditLog("DOCUMENT_CREATED", savedDocument, "Document was created");

        return DocumentResponse.from(savedDocument);
    }

    @GetMapping
    public List<DocumentResponse> listDocuments(
        @RequestParam UUID userId,
        @RequestParam(name = "name", required = false) String name
    ) {
        User user = findUser(userId);

        List<Document> documents;

        if (user.getRole() == UserRole.ADMIN) {
            documents = documentRepository.findAll();
        } else {
            List<Document> ownedDocuments = documentRepository.findByOwnerId(userId);

            List<UUID> sharedDocumentIds = documentAccessRepository.findByUserId(userId)
                .stream()
                .map(DocumentAccess::getDocumentId)
                .toList();

            List<Document> sharedDocuments = documentRepository.findAllById(sharedDocumentIds);

            documents = new ArrayList<>();
            documents.addAll(ownedDocuments);
            documents.addAll(sharedDocuments);
        }

        if (name != null && !name.isBlank()) {
            documents = documents.stream()
                .filter(document -> document.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
        }

        return documents.stream()
            .map(DocumentResponse::from)
            .toList();
    }

    @GetMapping("/{id}/access")
    public List<DocumentAccessResponse> listAccess(
        @PathVariable UUID id,
        @RequestParam UUID requesterId
    ) {
        User requester = findUser(requesterId);
        Document document = findDocument(id);

        if (!canManageAccess(requester, document)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return documentAccessRepository.findByDocumentId(id)
            .stream()
            .map(DocumentAccessResponse::from)
            .toList();
    }


    @PostMapping("/{id}/access")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentAccessResponse addAccess(
        @PathVariable UUID id,
        @RequestParam UUID requesterId,
        @RequestBody AddAccessRequest request
    ) {
        User requester = findUser(requesterId);
        Document document = findDocument(id);

        if (!canManageAccess(requester, document)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!userRepository.existsById(request.userId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (documentAccessRepository.existsByDocumentIdAndUserId(id, request.userId())) {
            throw new IllegalArgumentException("User already has access");
        }

        DocumentAccess access = DocumentAccess.create(
            id,
            request.userId(),
            request.accessType()
        );

        DocumentAccess savedAccess = documentAccessRepository.save(access);

        return DocumentAccessResponse.from(savedAccess);
    }

    @DeleteMapping("/{id}/access/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccess(
        @PathVariable UUID id,
        @PathVariable UUID userId,
        @RequestParam UUID requesterId
    ) {
        User requester = findUser(requesterId);
        Document document = findDocument(id);

        if (!canManageAccess(requester, document)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        documentAccessRepository.deleteByDocumentIdAndUserId(id, userId);
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable UUID id) {
        Document document = findDocument(id);

        return DocumentResponse.from(document);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id) throws MalformedURLException {
        Document document = findDocument(id);

        Path filePath = uploadDirectory.resolve(document.getStorageFileName());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        saveAuditLog("DOCUMENT_DOWNLOADED", document, "Document was downloaded");

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

    private Document findDocument(UUID id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private boolean canManageAccess(User user, Document document) {
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        return user.getId().equals(document.getOwnerId());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    public record DocumentResponse(
        UUID id,
        String name,
        String contentType,
        long size,
        UUID folderId,
        UUID ownerId,
        Instant createdAt
    ) {
        public static DocumentResponse from(Document document) {
            return new DocumentResponse(
                document.getId(),
                document.getName(),
                document.getContentType(),
                document.getSize(),
                document.getFolderId(),
                document.getOwnerId(),
                document.getCreatedAt()
            );
        }
    }

    public record AddAccessRequest(
        UUID userId,
        String accessType
    ) {
    }

    public record DocumentAccessResponse(
        UUID id,
        UUID documentId,
        UUID userId,
        String accessType,
        Instant createdAt
    ) {
        public static DocumentAccessResponse from(DocumentAccess access) {
            return new DocumentAccessResponse(
                access.getId(),
                access.getDocumentId(),
                access.getUserId(),
                access.getAccessType(),
                access.getCreatedAt()
            );
        }
    }

    public record ErrorResponse(String message) {
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable UUID id) throws IOException {
        Document document = findDocument(id);
        Path filePath = uploadDirectory.resolve(document.getStorageFileName());
        Files.deleteIfExists(filePath);

        documentRepository.deleteById(id);
        saveAuditLog("DOCUMENT_DELETED", document, "Document was deleted");
    }

    private void saveAuditLog(String eventType, Document document, String message) {
        AuditLog auditLog = AuditLog.create(
            eventType,
            "DOCUMENT",
            document.getId(),
            true,
            message
        );

        auditLogRepository.save(auditLog);
    }
}

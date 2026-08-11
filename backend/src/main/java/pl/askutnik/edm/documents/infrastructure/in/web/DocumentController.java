package pl.askutnik.edm.documents.infrastructure.in.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.infrastructure.out.persistence.AuditLogRepository;
import pl.askutnik.edm.audit.model.AuditLog;
import pl.askutnik.edm.documents.infrastructure.out.persistence.DocumentRepository;
import pl.askutnik.edm.documents.model.Document;
import pl.askutnik.edm.folders.infrastructure.out.persistence.FolderRepository;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final Path uploadDirectory;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;
    private final FolderRepository folderRepository;

    public DocumentController(
        DocumentRepository documentRepository,
        AuditLogRepository auditLogRepository,
        FolderRepository folderRepository,
        @Value("${app.upload-dir}") String uploadDirectory
    ) throws IOException {
        this.documentRepository = documentRepository;
        this.auditLogRepository = auditLogRepository;
        this.folderRepository = folderRepository;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDirectory);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam(name = "folderId", required = false) UUID folderId
    ) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (folderId != null && !folderRepository.existsById(folderId)) {
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
            folderId
        );

        Document savedDocument = documentRepository.save(document);
        saveAuditLog("DOCUMENT_CREATED", savedDocument, "Document was created");

        return DocumentResponse.from(savedDocument);
    }

    @GetMapping
    public List<DocumentResponse> listDocuments(@RequestParam(name = "name", required = false) String name) {
        List<Document> documents = findDocuments(name);

        return documents
            .stream()
            .map(DocumentResponse::from)
            .toList();
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

    private List<Document> findDocuments(String name) {
        if (name == null || name.isBlank()) {
            return documentRepository.findAll();
        }

        return documentRepository.findByNameContainingIgnoreCase(name);
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
        Instant createdAt
    ) {
        public static DocumentResponse from(Document document) {
            return new DocumentResponse(
                document.getId(),
                document.getName(),
                document.getContentType(),
                document.getSize(),
                document.getFolderId(),
                document.getCreatedAt()
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

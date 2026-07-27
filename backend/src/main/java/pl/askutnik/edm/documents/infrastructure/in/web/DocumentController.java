package pl.askutnik.edm.documents.infrastructure.in.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

import pl.askutnik.edm.documents.infrastructure.out.persistence.DocumentRepository;
import pl.askutnik.edm.documents.model.Document;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final Path uploadDirectory = Path.of("uploads");
    private final DocumentRepository documentRepository;

    public DocumentController(DocumentRepository documentRepository) throws IOException {
        this.documentRepository = documentRepository;
        Files.createDirectories(uploadDirectory);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
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
            storageFileName
        );

        return DocumentResponse.from(documentRepository.save(document));
    }

    @GetMapping
    public List<DocumentResponse> listDocuments(@RequestParam(required = false) String name) {
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
        Instant createdAt
    ) {
        public static DocumentResponse from(Document document) {
            return new DocumentResponse(
                document.getId(),
                document.getName(),
                document.getContentType(),
                document.getSize(),
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
    }
}

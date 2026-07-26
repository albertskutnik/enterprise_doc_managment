package pl.askutnik.edm.documents.infrastructure.in.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pl.askutnik.edm.documents.infrastructure.out.persistence.InMemoryDocumentRepository;
import pl.askutnik.edm.documents.model.Document;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final InMemoryDocumentRepository documentRepository;

    public DocumentController(InMemoryDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(@RequestBody CreateDocumentRequest request) {
        Document document = Document.create(
            request.name(),
            request.contentType(),
            request.size()
        );

        return DocumentResponse.from(documentRepository.save(document));
    }

    @GetMapping
    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll()
            .stream()
            .map(DocumentResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable UUID id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return DocumentResponse.from(document);
    }

    public record CreateDocumentRequest(
        String name,
        String contentType,
        long size
    ) {
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
                document.id(),
                document.name(),
                document.contentType(),
                document.size(),
                document.createdAt()
            );
        }
    }
}

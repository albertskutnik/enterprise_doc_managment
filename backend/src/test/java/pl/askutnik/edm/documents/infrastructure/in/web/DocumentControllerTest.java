package pl.askutnik.edm.documents.infrastructure.in.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.documents.infrastructure.in.web.DocumentController.DocumentResponse;
import pl.askutnik.edm.documents.infrastructure.out.persistence.DocumentRepository;
import pl.askutnik.edm.documents.model.Document;

class DocumentControllerTest {

    private List<Document> documents;
    private DocumentRepository documentRepository;
    private DocumentController documentController;

    @BeforeEach
    void setUp() throws IOException {
        documents = new ArrayList<>();
        documentRepository = mock(DocumentRepository.class);

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            documents.add(document);
            return document;
        });

        when(documentRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(documents));

        when(documentRepository.findByNameContainingIgnoreCase(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> document.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
        });

        when(documentRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> document.getId().equals(id))
                .findFirst();
        });

        doAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            documents.removeIf(document -> document.getId().equals(id));

            return null;
        }).when(documentRepository).deleteById(any(UUID.class));

        documentController = new DocumentController(documentRepository);
    }

    @AfterEach
    void cleanUp() throws IOException {
        for (Document document : documents) {
            Files.deleteIfExists(Path.of("uploads").resolve(document.getStorageFileName()));
        }
    }

    @Test
    void shouldCreateDocumentFromUploadedFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );

        DocumentResponse response = documentController.createDocument(file);

        assertNotNull(response.id());
        assertEquals("test.txt", response.name());
        assertEquals("text/plain", response.contentType());
        assertEquals(5, response.size());
        assertNotNull(response.createdAt());
        assertEquals(1, documents.size());
    }

    @Test
    void shouldListDocuments() throws IOException {
        MockMultipartFile firstFile = new MockMultipartFile(
            "file",
            "first.txt",
            "text/plain",
            "first".getBytes()
        );
        MockMultipartFile secondFile = new MockMultipartFile(
            "file",
            "second.txt",
            "text/plain",
            "second".getBytes()
        );

        documentController.createDocument(firstFile);
        documentController.createDocument(secondFile);

        assertEquals(2, documentController.listDocuments(null).size());
    }

    @Test
    void shouldSearchDocumentsByName() throws IOException {
        MockMultipartFile firstFile = new MockMultipartFile(
            "file",
            "invoice.txt",
            "text/plain",
            "invoice".getBytes()
        );
        MockMultipartFile secondFile = new MockMultipartFile(
            "file",
            "contract.txt",
            "text/plain",
            "contract".getBytes()
        );

        documentController.createDocument(firstFile);
        documentController.createDocument(secondFile);

        List<DocumentResponse> foundDocuments = documentController.listDocuments("voice");

        assertEquals(1, foundDocuments.size());
        assertEquals("invoice.txt", foundDocuments.get(0).name());
    }

    @Test
    void shouldGetDocumentById() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file);

        DocumentResponse foundDocument = documentController.getDocumentById(createdDocument.id());

        assertEquals(createdDocument.id(), foundDocument.id());
        assertEquals("test.txt", foundDocument.name());
    }

    @Test
    void shouldReturnNotFoundWhenDocumentDoesNotExist() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> documentController.getDocumentById(UUID.randomUUID())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldDownloadDocument() throws IOException {
        byte[] content = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content
        );
        DocumentResponse createdDocument = documentController.createDocument(file);

        ResponseEntity<Resource> response = documentController.downloadDocument(createdDocument.id());
        Resource resource = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("text/plain", response.getHeaders().getContentType().toString());
        assertNotNull(resource);
        assertArrayEquals(content, resource.getInputStream().readAllBytes());
    }

    @Test
    void shouldDeleteDocument() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file);

        documentController.deleteDocument(createdDocument.id());

        assertEquals(0, documents.size());
        assertThrows(
            ResponseStatusException.class,
            () -> documentController.getDocumentById(createdDocument.id())
        );
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentController.createDocument(file)
        );

        assertEquals("File cannot be empty", exception.getMessage());
    }
}

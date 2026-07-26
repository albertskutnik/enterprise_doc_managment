package pl.askutnik.edm.documents.infrastructure.in.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import pl.askutnik.edm.documents.infrastructure.out.persistence.InMemoryDocumentRepository;

class DocumentControllerTest {

    private InMemoryDocumentRepository documentRepository;
    private DocumentController documentController;

    @BeforeEach
    void setUp() throws IOException {
        documentRepository = new InMemoryDocumentRepository();
        documentController = new DocumentController(documentRepository);
    }

    @AfterEach
    void cleanUp() throws IOException {
        for (var document : documentRepository.findAll()) {
            Files.deleteIfExists(Path.of("uploads").resolve(document.storageFileName()));
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
        assertEquals(1, documentRepository.findAll().size());
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

        assertEquals(2, documentController.listDocuments().size());
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("text/plain", response.getHeaders().getContentType().toString());
        assertArrayEquals(content, response.getBody().getInputStream().readAllBytes());
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

        assertEquals(0, documentRepository.findAll().size());
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

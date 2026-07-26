package pl.askutnik.edm.documents.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void shouldCreateDocument() {
        Document document = Document.create(
            "test.pdf",
            "application/pdf",
            1234,
            "stored-test.pdf"
        );

        assertNotNull(document.id());
        assertEquals("test.pdf", document.name());
        assertEquals("application/pdf", document.contentType());
        assertEquals(1234, document.size());
        assertEquals("stored-test.pdf", document.storageFileName());
        assertNotNull(document.createdAt());
    }

    @Test
    void shouldRejectEmptyName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Document.create(
                "",
                "application/pdf",
                1234,
                "stored-test.pdf"
            )
        );

        assertEquals("Document name cannot be empty", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidSize() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Document.create(
                "test.pdf",
                "application/pdf",
                0,
                "stored-test.pdf"
            )
        );

        assertEquals("Document size must be greater than zero", exception.getMessage());
    }
}

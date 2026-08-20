package pl.askutnik.edm.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void shouldCreateDocument() {
        Document document = Document.create(
            "test.pdf",
            "application/pdf",
            1234,
            "stored-test.pdf",
            null
        );

        assertNotNull(document.getId());
        assertEquals("test.pdf", document.getName());
        assertEquals("application/pdf", document.getContentType());
        assertEquals(1234, document.getSize());
        assertEquals("stored-test.pdf", document.getStorageFileName());
        assertNull(document.getFolderId());
        assertNotNull(document.getCreatedAt());
    }

    @Test
    void shouldRejectEmptyName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Document.create(
                "",
                "application/pdf",
                1234,
                "stored-test.pdf",
                null
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
                "stored-test.pdf",
                null
            )
        );

        assertEquals("Document size must be greater than zero", exception.getMessage());
    }
}

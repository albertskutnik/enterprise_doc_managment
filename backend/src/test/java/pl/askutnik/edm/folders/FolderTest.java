package pl.askutnik.edm.folders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FolderTest {

    @Test
    void shouldCreateFolder() {
        Folder folder = Folder.create("Invoices");

        assertNotNull(folder.getId());
        assertEquals("Invoices", folder.getName());
        assertNotNull(folder.getCreatedAt());
    }

    @Test
    void shouldRejectEmptyName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Folder.create("")
        );

        assertEquals("Folder name cannot be empty", exception.getMessage());
    }
}

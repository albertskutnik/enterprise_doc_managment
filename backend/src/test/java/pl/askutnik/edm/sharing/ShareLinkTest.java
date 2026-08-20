package pl.askutnik.edm.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ShareLinkTest {

    @Test
    void shouldCreateShareLink() {
        UUID documentId = UUID.randomUUID();

        ShareLink shareLink = ShareLink.create(
            documentId,
            Instant.now().plusSeconds(3600)
        );

        assertNotNull(shareLink.getId());
        assertNotNull(shareLink.getToken());
        assertEquals(documentId, shareLink.getDocumentId());
        assertNotNull(shareLink.getExpiresAt());
        assertNotNull(shareLink.getCreatedAt());
    }
}

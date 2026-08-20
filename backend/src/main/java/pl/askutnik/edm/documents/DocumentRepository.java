package pl.askutnik.edm.documents;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByNameContainingIgnoreCase(String name);

    List<Document> findByFolderId(UUID folderId);
}

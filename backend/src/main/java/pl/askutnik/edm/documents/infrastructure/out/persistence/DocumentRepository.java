package pl.askutnik.edm.documents.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.askutnik.edm.documents.model.Document;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByNameContainingIgnoreCase(String name);

    List<Document> findByFolderId(UUID folderId);
}

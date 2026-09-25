package pl.askutnik.edm.documents;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentAccessRepository extends JpaRepository<DocumentAccess, UUID> {

    List<DocumentAccess> findByDocumentId(UUID documentId);

    List<DocumentAccess> findByUserId(UUID userId);

    boolean existsByDocumentIdAndUserId(UUID documentId, UUID userId);

    @Transactional
    void deleteByDocumentIdAndUserId(UUID documentId, UUID userId);

    @Transactional
    void deleteByDocumentId(UUID documentId);
}

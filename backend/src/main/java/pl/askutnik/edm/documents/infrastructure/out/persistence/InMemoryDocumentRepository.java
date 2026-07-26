package pl.askutnik.edm.documents.infrastructure.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import pl.askutnik.edm.documents.model.Document;

@Repository
public class InMemoryDocumentRepository {

    private final List<Document> documents = new ArrayList<>();

    public Document save(Document document) {
        documents.add(document);
        return document;
    }

    public List<Document> findAll() {
        return new ArrayList<>(documents);
    }

    public Optional<Document> findById(UUID id) {
        return documents.stream()
            .filter(document -> document.id().equals(id))
            .findFirst();
    }
}

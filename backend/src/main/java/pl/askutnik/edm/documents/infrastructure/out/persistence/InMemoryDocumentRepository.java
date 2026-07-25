package pl.askutnik.edm.documents.infrastructure.out.persistence;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import pl.askutnik.edm.documents.model.Document;
import pl.askutnik.edm.documents.usecase.port.out.DocumentRepository;

@Repository
public class InMemoryDocumentRepository implements DocumentRepository {

    private final List<Document> documents = new ArrayList<>();

    @Override
    public Document save(Document document) {
        documents.add(document);
        return document;
    }

    @Override
    public List<Document> findAll() {
        return new ArrayList<>(documents);
    }
}

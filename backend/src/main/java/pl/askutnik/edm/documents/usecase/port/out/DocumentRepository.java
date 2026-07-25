package pl.askutnik.edm.documents.usecase.port.out;

import java.util.List;

import pl.askutnik.edm.documents.model.Document;

public interface DocumentRepository {
    Document save(Document document);

    List<Document> findAll();
}

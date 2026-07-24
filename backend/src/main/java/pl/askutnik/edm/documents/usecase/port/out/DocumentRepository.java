package pl.askutnik.edm.documents.usecase.port.out;

import pl.askutnik.edm.documents.model.Document;

public interface DocumentRepository {
    Document save(Document document);
}

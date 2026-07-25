package pl.askutnik.edm.documents.usecase.port.in;

import java.util.List;

import pl.askutnik.edm.documents.model.Document;

public interface ListDocumentsUseCase {
    List<Document> listDocuments();
}

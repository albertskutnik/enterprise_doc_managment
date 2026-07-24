package pl.askutnik.edm.documents.usecase.port.in;

import pl.askutnik.edm.documents.model.Document;

public interface CreateDocumentUseCase {
    Document createDocument(CreateDocumentCommand command);
}

package pl.askutnik.edm.documents.usecase.service;

import org.springframework.stereotype.Service;

import pl.askutnik.edm.documents.model.Document;
import pl.askutnik.edm.documents.usecase.port.in.CreateDocumentCommand;
import pl.askutnik.edm.documents.usecase.port.in.CreateDocumentUseCase;
import pl.askutnik.edm.documents.usecase.port.out.DocumentRepository;

@Service
public class CreateDocumentService implements CreateDocumentUseCase {
    private final DocumentRepository documentRepository;

    public CreateDocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document createDocument(CreateDocumentCommand command) {
        Document document = Document.create(
            command.getName(),
            command.getContentType(),
            command.getSize()
        );

        return documentRepository.save(document);
    }
}

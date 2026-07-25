package pl.askutnik.edm.documents.usecase.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pl.askutnik.edm.documents.model.Document;
import pl.askutnik.edm.documents.usecase.port.in.ListDocumentsUseCase;
import pl.askutnik.edm.documents.usecase.port.out.DocumentRepository;

@Service
public class ListDocumentsService implements ListDocumentsUseCase {

    private final DocumentRepository documentRepository;

    public ListDocumentsService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public List<Document> listDocuments() {
        return documentRepository.findAll();
    }
}

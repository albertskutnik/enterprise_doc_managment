package pl.askutnik.edm.documents.infrastructure.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pl.askutnik.edm.documents.model.Document;
import pl.askutnik.edm.documents.usecase.port.in.CreateDocumentCommand;
import pl.askutnik.edm.documents.usecase.port.in.CreateDocumentUseCase;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private CreateDocumentUseCase createDocumentUseCase;

    public DocumentController(CreateDocumentUseCase createDocumentUseCase) {
        this.createDocumentUseCase = createDocumentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(@RequestBody CreateDocumentRequest request) {
        CreateDocumentCommand command = new CreateDocumentCommand(
            request.getName(),
            request.getContentType(),
            request.getSize()
        );

        Document document = createDocumentUseCase.createDocument(command);

        return DocumentResponse.from(document);
    }


}
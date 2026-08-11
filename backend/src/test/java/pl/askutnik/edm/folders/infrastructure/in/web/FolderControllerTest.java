package pl.askutnik.edm.folders.infrastructure.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.infrastructure.out.persistence.AuditLogRepository;
import pl.askutnik.edm.audit.model.AuditLog;
import pl.askutnik.edm.documents.infrastructure.out.persistence.DocumentRepository;
import pl.askutnik.edm.documents.model.Document;
import pl.askutnik.edm.folders.infrastructure.in.web.FolderController.CreateFolderRequest;
import pl.askutnik.edm.folders.infrastructure.in.web.FolderController.FolderResponse;
import pl.askutnik.edm.folders.infrastructure.out.persistence.FolderRepository;
import pl.askutnik.edm.folders.model.Folder;

class FolderControllerTest {

    private List<Folder> folders;
    private List<Document> documents;
    private List<AuditLog> auditLogs;
    private FolderController folderController;

    @BeforeEach
    void setUp() {
        folders = new ArrayList<>();
        documents = new ArrayList<>();
        auditLogs = new ArrayList<>();
        FolderRepository folderRepository = mock(FolderRepository.class);
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);

        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> {
            Folder folder = invocation.getArgument(0);
            folders.add(folder);
            return folder;
        });

        when(folderRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(folders));

        when(folderRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);

            return folders.stream()
                .filter(folder -> folder.getId().equals(id))
                .findFirst();
        });

        when(documentRepository.findByFolderId(any(UUID.class))).thenAnswer(invocation -> {
            UUID folderId = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> folderId.equals(document.getFolderId()))
                .toList();
        });

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            auditLogs.add(auditLog);
            return auditLog;
        });

        folderController = new FolderController(
            folderRepository,
            documentRepository,
            auditLogRepository
        );
    }

    @Test
    void shouldCreateFolder() {
        FolderResponse response = folderController.createFolder(new CreateFolderRequest("Invoices"));

        assertNotNull(response.id());
        assertEquals("Invoices", response.name());
        assertNotNull(response.createdAt());
        assertEquals(1, folders.size());
        assertEquals(1, auditLogs.size());
        assertEquals("FOLDER_CREATED", auditLogs.get(0).getEventType());
    }

    @Test
    void shouldListFolders() {
        Folder firstFolder = Folder.create("Invoices");
        Folder secondFolder = Folder.create("Contracts");
        folders.add(firstFolder);
        folders.add(secondFolder);

        List<FolderResponse> response = folderController.listFolders();

        assertEquals(2, response.size());
    }

    @Test
    void shouldGetFolderById() {
        Folder folder = Folder.create("Invoices");
        folders.add(folder);

        FolderResponse response = folderController.getFolderById(folder.getId());

        assertEquals(folder.getId(), response.id());
        assertEquals("Invoices", response.name());
    }

    @Test
    void shouldReturnNotFoundWhenFolderDoesNotExist() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> folderController.getFolderById(UUID.randomUUID())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldListFolderDocuments() {
        Folder folder = Folder.create("Invoices");
        folders.add(folder);
        documents.add(Document.create(
            "invoice.txt",
            "text/plain",
            123,
            "stored-invoice.txt",
            folder.getId()
        ));
        documents.add(Document.create(
            "contract.txt",
            "text/plain",
            123,
            "stored-contract.txt",
            UUID.randomUUID()
        ));

        var response = folderController.listFolderDocuments(folder.getId());

        assertEquals(1, response.size());
        assertEquals("invoice.txt", response.get(0).name());
    }
}

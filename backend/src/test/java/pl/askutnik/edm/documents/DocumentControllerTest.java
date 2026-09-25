package pl.askutnik.edm.documents;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.AuditLog;
import pl.askutnik.edm.audit.AuditLogRepository;
import pl.askutnik.edm.documents.DocumentController.AddAccessRequest;
import pl.askutnik.edm.documents.DocumentController.DocumentAccessResponse;
import pl.askutnik.edm.documents.DocumentController.DocumentResponse;
import pl.askutnik.edm.folders.Folder;
import pl.askutnik.edm.folders.FolderRepository;
import pl.askutnik.edm.users.User;
import pl.askutnik.edm.users.UserRepository;
import pl.askutnik.edm.users.UserRole;

class DocumentControllerTest {

    private List<Document> documents;
    private List<DocumentAccess> documentAccesses;
    private List<AuditLog> auditLogs;
    private List<Folder> folders;
    private List<User> users;
    private DocumentRepository documentRepository;
    private DocumentAccessRepository documentAccessRepository;
    private AuditLogRepository auditLogRepository;
    private FolderRepository folderRepository;
    private UserRepository userRepository;
    private DocumentController documentController;
    private User owner;
    private User otherUser;
    private User admin;

    @TempDir
    private Path uploadDirectory;

    @BeforeEach
    void setUp() throws IOException {
        documents = new ArrayList<>();
        documentAccesses = new ArrayList<>();
        auditLogs = new ArrayList<>();
        folders = new ArrayList<>();
        users = new ArrayList<>();
        owner = User.create("owner@test.pl", "password", UserRole.USER);
        otherUser = User.create("other@test.pl", "password", UserRole.USER);
        admin = User.create("admin@test.pl", "password", UserRole.ADMIN);
        users.add(owner);
        users.add(otherUser);
        users.add(admin);
        documentRepository = mock(DocumentRepository.class);
        documentAccessRepository = mock(DocumentAccessRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        folderRepository = mock(FolderRepository.class);
        userRepository = mock(UserRepository.class);

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            documents.add(document);
            return document;
        });

        when(documentRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(documents));

        when(documentRepository.findByNameContainingIgnoreCase(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> document.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
        });

        when(documentRepository.findByFolderId(any(UUID.class))).thenAnswer(invocation -> {
            UUID folderId = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> folderId.equals(document.getFolderId()))
                .toList();
        });

        when(documentRepository.findByOwnerId(any(UUID.class))).thenAnswer(invocation -> {
            UUID ownerId = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> ownerId.equals(document.getOwnerId()))
                .toList();
        });

        when(documentRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<UUID> ids = invocation.getArgument(0);
            List<UUID> documentIds = new ArrayList<>();
            ids.forEach(documentIds::add);

            return documents.stream()
                .filter(document -> documentIds.contains(document.getId()))
                .toList();
        });

        when(documentRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);

            return documents.stream()
                .filter(document -> document.getId().equals(id))
                .findFirst();
        });

        when(documentAccessRepository.save(any(DocumentAccess.class))).thenAnswer(invocation -> {
            DocumentAccess documentAccess = invocation.getArgument(0);
            documentAccesses.add(documentAccess);
            return documentAccess;
        });

        when(documentAccessRepository.findByUserId(any(UUID.class))).thenAnswer(invocation -> {
            UUID userId = invocation.getArgument(0);

            return documentAccesses.stream()
                .filter(documentAccess -> userId.equals(documentAccess.getUserId()))
                .toList();
        });

        when(documentAccessRepository.findByDocumentId(any(UUID.class))).thenAnswer(invocation -> {
            UUID documentId = invocation.getArgument(0);

            return documentAccesses.stream()
                .filter(documentAccess -> documentId.equals(documentAccess.getDocumentId()))
                .toList();
        });

        when(documentAccessRepository.existsByDocumentIdAndUserId(any(UUID.class), any(UUID.class))).thenAnswer(invocation -> {
            UUID documentId = invocation.getArgument(0);
            UUID userId = invocation.getArgument(1);

            return documentAccesses.stream()
                .anyMatch(documentAccess ->
                    documentId.equals(documentAccess.getDocumentId()) && userId.equals(documentAccess.getUserId())
                );
        });

        doAnswer(invocation -> {
            UUID documentId = invocation.getArgument(0);
            UUID userId = invocation.getArgument(1);
            documentAccesses.removeIf(documentAccess ->
                documentId.equals(documentAccess.getDocumentId()) && userId.equals(documentAccess.getUserId())
            );

            return null;
        }).when(documentAccessRepository).deleteByDocumentIdAndUserId(any(UUID.class), any(UUID.class));

        doAnswer(invocation -> {
            UUID documentId = invocation.getArgument(0);
            documentAccesses.removeIf(documentAccess -> documentId.equals(documentAccess.getDocumentId()));

            return null;
        }).when(documentAccessRepository).deleteByDocumentId(any(UUID.class));

        doAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            documents.removeIf(document -> document.getId().equals(id));

            return null;
        }).when(documentRepository).deleteById(any(UUID.class));

        when(folderRepository.existsById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);

            return folders.stream()
                .anyMatch(folder -> folder.getId().equals(id));
        });

        when(userRepository.existsById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);

            return users.stream()
                .anyMatch(user -> user.getId().equals(id));
        });

        when(userRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);

            return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
        });

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            auditLogs.add(auditLog);
            return auditLog;
        });

        documentController = new DocumentController(
            documentRepository,
            auditLogRepository,
            folderRepository,
            documentAccessRepository,
            userRepository,
            uploadDirectory.toString()
        );
    }

    @AfterEach
    void cleanUp() {
        documents.clear();
        documentAccesses.clear();
        auditLogs.clear();
        folders.clear();
        users.clear();
    }

    @Test
    void shouldCreateDocumentFromUploadedFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );

        DocumentResponse response = documentController.createDocument(file, owner.getId(), null);

        assertNotNull(response.id());
        assertEquals("test.txt", response.name());
        assertEquals("text/plain", response.contentType());
        assertEquals(5, response.size());
        assertEquals(owner.getId(), response.ownerId());
        assertNotNull(response.createdAt());
        assertEquals(1, documents.size());
        assertEquals(1, auditLogs.size());
        assertEquals("DOCUMENT_CREATED", auditLogs.get(0).getEventType());
    }

    @Test
    void shouldListDocuments() throws IOException {
        MockMultipartFile firstFile = new MockMultipartFile(
            "file",
            "first.txt",
            "text/plain",
            "first".getBytes()
        );
        MockMultipartFile secondFile = new MockMultipartFile(
            "file",
            "second.txt",
            "text/plain",
            "second".getBytes()
        );

        documentController.createDocument(firstFile, owner.getId(), null);
        documentController.createDocument(secondFile, owner.getId(), null);

        assertEquals(2, documentController.listDocuments(owner.getId(), null).size());
    }

    @Test
    void shouldSearchDocumentsByName() throws IOException {
        MockMultipartFile firstFile = new MockMultipartFile(
            "file",
            "invoice.txt",
            "text/plain",
            "invoice".getBytes()
        );
        MockMultipartFile secondFile = new MockMultipartFile(
            "file",
            "contract.txt",
            "text/plain",
            "contract".getBytes()
        );

        documentController.createDocument(firstFile, owner.getId(), null);
        documentController.createDocument(secondFile, owner.getId(), null);

        List<DocumentResponse> foundDocuments = documentController.listDocuments(owner.getId(), "voice");

        assertEquals(1, foundDocuments.size());
        assertEquals("invoice.txt", foundDocuments.get(0).name());
    }

    @Test
    void shouldCreateDocumentInFolder() throws IOException {
        Folder folder = Folder.create("Invoices");
        folders.add(folder);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "invoice.txt",
            "text/plain",
            "invoice".getBytes()
        );

        DocumentResponse response = documentController.createDocument(file, owner.getId(), folder.getId());

        assertEquals(folder.getId(), response.folderId());
        assertEquals(folder.getId(), documents.get(0).getFolderId());
    }

    @Test
    void shouldReturnNotFoundWhenFolderDoesNotExistDuringUpload() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "invoice.txt",
            "text/plain",
            "invoice".getBytes()
        );

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> documentController.createDocument(file, owner.getId(), UUID.randomUUID())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldGetDocumentById() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);

        DocumentResponse foundDocument = documentController.getDocumentById(createdDocument.id(), owner.getId());

        assertEquals(createdDocument.id(), foundDocument.id());
        assertEquals("test.txt", foundDocument.name());
    }

    @Test
    void shouldReturnNotFoundWhenDocumentDoesNotExist() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> documentController.getDocumentById(UUID.randomUUID(), owner.getId())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldReturnForbiddenWhenUserDoesNotHaveAccessToDocument() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> documentController.getDocumentById(createdDocument.id(), otherUser.getId())
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldAllowUserWithDocumentAccessToGetDocument() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);
        documentController.addAccess(
            createdDocument.id(),
            owner.getId(),
            new AddAccessRequest(otherUser.getId(), "READ")
        );

        DocumentResponse foundDocument = documentController.getDocumentById(createdDocument.id(), otherUser.getId());

        assertEquals(createdDocument.id(), foundDocument.id());
    }

    @Test
    void shouldDownloadDocument() throws IOException {
        byte[] content = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);

        ResponseEntity<Resource> response = documentController.downloadDocument(createdDocument.id(), owner.getId());
        Resource resource = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("text/plain", response.getHeaders().getContentType().toString());
        assertNotNull(resource);
        assertArrayEquals(content, resource.getInputStream().readAllBytes());
        assertEquals(2, auditLogs.size());
        assertEquals("DOCUMENT_DOWNLOADED", auditLogs.get(1).getEventType());
    }

    @Test
    void shouldDeleteDocument() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);

        documentController.deleteDocument(createdDocument.id(), owner.getId());

        assertEquals(0, documents.size());
        assertEquals(2, auditLogs.size());
        assertEquals("DOCUMENT_DELETED", auditLogs.get(1).getEventType());
        assertThrows(
            ResponseStatusException.class,
            () -> documentController.getDocumentById(createdDocument.id(), owner.getId())
        );
    }

    @Test
    void shouldAllowAdminToDeleteDocument() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);

        documentController.deleteDocument(createdDocument.id(), admin.getId());

        assertEquals(0, documents.size());
    }

    @Test
    void shouldReturnForbiddenWhenUserTriesToDeleteOtherUserDocument() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> documentController.deleteDocument(createdDocument.id(), otherUser.getId())
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldAddAndDeleteDocumentAccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
        );
        DocumentResponse createdDocument = documentController.createDocument(file, owner.getId(), null);

        DocumentAccessResponse access = documentController.addAccess(
            createdDocument.id(),
            owner.getId(),
            new AddAccessRequest(otherUser.getId(), "READ")
        );

        assertNotNull(access.id());
        assertEquals(otherUser.getId(), access.userId());
        assertEquals(1, documentAccesses.size());

        documentController.deleteAccess(createdDocument.id(), otherUser.getId(), owner.getId());

        assertEquals(0, documentAccesses.size());
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentController.createDocument(file, owner.getId(), null)
        );

        assertEquals("File cannot be empty", exception.getMessage());
    }
}

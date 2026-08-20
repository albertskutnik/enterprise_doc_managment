package pl.askutnik.edm.folders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.AuditLog;
import pl.askutnik.edm.audit.AuditLogRepository;
import pl.askutnik.edm.documents.DocumentController.DocumentResponse;
import pl.askutnik.edm.documents.DocumentRepository;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;

    public FolderController(
        FolderRepository folderRepository,
        DocumentRepository documentRepository,
        AuditLogRepository auditLogRepository
    ) {
        this.folderRepository = folderRepository;
        this.documentRepository = documentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponse createFolder(@RequestBody CreateFolderRequest request) {
        Folder folder = Folder.create(request.name());
        Folder savedFolder = folderRepository.save(folder);

        saveAuditLog("FOLDER_CREATED", savedFolder, "Folder was created");

        return FolderResponse.from(savedFolder);
    }

    @GetMapping
    public List<FolderResponse> listFolders() {
        return folderRepository.findAll()
            .stream()
            .map(FolderResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public FolderResponse getFolderById(@PathVariable UUID id) {
        Folder folder = findFolder(id);

        return FolderResponse.from(folder);
    }

    @GetMapping("/{id}/documents")
    public List<DocumentResponse> listFolderDocuments(@PathVariable UUID id) {
        findFolder(id);

        return documentRepository.findByFolderId(id)
            .stream()
            .map(DocumentResponse::from)
            .toList();
    }

    private Folder findFolder(UUID id) {
        return folderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void saveAuditLog(String eventType, Folder folder, String message) {
        AuditLog auditLog = AuditLog.create(
            eventType,
            "FOLDER",
            folder.getId(),
            true,
            message
        );

        auditLogRepository.save(auditLog);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    public record CreateFolderRequest(String name) {
    }

    public record FolderResponse(
        UUID id,
        String name,
        Instant createdAt
    ) {
        public static FolderResponse from(Folder folder) {
            return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt()
            );
        }
    }

    public record ErrorResponse(String message) {
    }
}

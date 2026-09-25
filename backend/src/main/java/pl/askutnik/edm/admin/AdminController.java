package pl.askutnik.edm.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.askutnik.edm.audit.AuditLogController;
import pl.askutnik.edm.audit.AuditLogRepository;
import pl.askutnik.edm.documents.Document;
import pl.askutnik.edm.documents.DocumentAccessRepository;
import pl.askutnik.edm.documents.DocumentController;
import pl.askutnik.edm.documents.DocumentRepository;
import pl.askutnik.edm.users.User;
import pl.askutnik.edm.users.UserController;
import pl.askutnik.edm.users.UserRepository;
import pl.askutnik.edm.users.UserRole;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final Path uploadDirectory;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentAccessRepository documentAccessRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(
        UserRepository userRepository,
        DocumentRepository documentRepository,
        DocumentAccessRepository documentAccessRepository,
        AuditLogRepository auditLogRepository,
        @Value("${app.upload-dir}") String uploadDirectory
    ) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentAccessRepository = documentAccessRepository;
        this.auditLogRepository = auditLogRepository;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @GetMapping("/users")
    public List<UserController.UserResponse> listUsers(@RequestParam UUID adminId) {
        findAdmin(adminId);

        return userRepository.findAll()
            .stream()
            .map(UserController.UserResponse::from)
            .toList();
    }

    @GetMapping("/documents")
    public List<DocumentController.DocumentResponse> listDocuments(@RequestParam UUID adminId) {
        findAdmin(adminId);

        return documentRepository.findAll()
            .stream()
            .map(DocumentController.DocumentResponse::from)
            .toList();
    }

    @GetMapping("/audit-logs")
    public List<AuditLogController.AuditLogResponse> listAuditLogs(@RequestParam UUID adminId) {
        findAdmin(adminId);

        return auditLogRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(AuditLogController.AuditLogResponse::from)
            .toList();
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
        @PathVariable UUID id,
        @RequestParam UUID adminId
    ) {
        findAdmin(adminId);
        userRepository.deleteById(id);
    }

    @DeleteMapping("/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(
        @PathVariable UUID id,
        @RequestParam UUID adminId
    ) throws IOException {
        findAdmin(adminId);

        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Path filePath = uploadDirectory.resolve(document.getStorageFileName());
        Files.deleteIfExists(filePath);

        documentAccessRepository.deleteByDocumentId(id);
        documentRepository.deleteById(id);
    }

    private User findAdmin(UUID adminId) {
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return admin;
    }
}

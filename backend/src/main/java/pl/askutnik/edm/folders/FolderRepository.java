package pl.askutnik.edm.folders;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
}

package pl.askutnik.edm.folders.infrastructure.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.askutnik.edm.folders.model.Folder;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
}

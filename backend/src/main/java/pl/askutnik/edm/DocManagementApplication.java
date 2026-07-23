package pl.askutnik.edm;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocManagementApplication {

    private final Path uploadDirectory;

    public DocManagementApplication(@Value("{app.upload-dir}") String uploadDirectory) throws Exception {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDirectory);
    }
    


    public static void main(String[] args) {
        SpringApplication.run(DocManagementApplication.class, args);
    }


    

}
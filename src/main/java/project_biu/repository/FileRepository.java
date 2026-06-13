package project_biu.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * A file Repository to manage files, keyed by the file name.
 */
public interface FileRepository {
    void save(String fileName, String content) throws IOException;

    boolean exists(String fileName);

    Optional<Path> location(String fileName);

    List<String> getAll() throws IOException;

    void delete(String fileName) throws IOException;

    void deleteAll() throws IOException;
}
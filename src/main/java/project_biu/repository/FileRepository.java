package project_biu.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * A file Repository to manage files, keyed by the file name.
 *
 * <p>Implementations decide where and how the files are kept; callers only refer to
 * them by name. Useful for persisting uploaded artifacts (e.g., configuration files)
 * so they can be re-read later without re-uploading.
 */
public interface FileRepository {
    /**
     * Creates or overwrites the named file with the given content.
     */
    void save(String fileName, String content);

    boolean exists(String fileName);

    /**
     * The path of the named file (whether it currently exists).
     */
    Optional<Path> location(String fileName);

    /**
     * Names of all stored files.
     */
    List<String> getAll();

    void delete(String fileName);

    void deleteAll();
}
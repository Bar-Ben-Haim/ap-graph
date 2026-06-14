package project_biu.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * A file Repository to manage files, keyed by the file name.
 */
public interface FileRepository {
    /**
     * Saves the provided content into a file with the given name.
     *
     * @param fileName The name of the file where the content will be saved.
     * @param content  The content to write to the specified file.
     * @throws IOException If an I/O error occurs during the save operation.
     */
    void save(String fileName, String content) throws IOException;

    /**
     * Checks if a file with the given name exists.
     *
     * @param fileName The name of the file to check.
     * @return true if the file exists, false otherwise.
     */
    boolean exists(String fileName);

    /**
     * Gets the full path of a file by its name.
     *
     * @param fileName The name of the file.
     * @return The full path of the file, or an empty optional if the file does not exist.
     */
    Optional<Path> location(String fileName);

    /**
     * Returns all the files in the repository.
     *
     * @return All the files in the repository.
     * @throws IOException If an I/O error occurs while reading the file names.
     */
    List<String> getAll() throws IOException;

    /**
     * Deletes a file from the repository by the file name.
     *
     * @param fileName The file name to delete.
     * @throws IOException If an I/O error occurs while deleting the file.
     */
    void delete(String fileName) throws IOException;

    /**
     * Deletes all the files in the repository.
     *
     * @throws IOException If an I/O error occurs while deleting the files.
     */
    void deleteAll() throws IOException;
}
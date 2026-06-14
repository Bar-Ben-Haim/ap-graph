package project_biu.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Stores files inside a single application-owned directory. The directory is created
 * on construction and is the sole location used for managing files.
 */
public class LocalFileRepository implements FileRepository {
    private final Path baseDirectory;

    public LocalFileRepository(Path baseDirectory) throws IOException {
        this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
        Files.createDirectories(this.baseDirectory);
    }

    @Override
    public synchronized void save(String fileName, String content) throws IOException {
        final Path target = resolve(fileName).orElseThrow(() -> new RuntimeException("Could not resolve file: " + fileName));
        Files.createDirectories(baseDirectory);
        Files.writeString(target, content);

    }

    @Override
    public synchronized boolean exists(String fileName) {
        return resolve(fileName).filter(Files::isRegularFile).map(Files::exists).orElse(false);
    }

    @Override
    public synchronized Optional<Path> location(String fileName) {
        return resolve(fileName).filter(Files::isRegularFile);
    }

    @Override
    public synchronized List<String> getAll() throws IOException {
        if (!Files.isDirectory(baseDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(baseDirectory)) {
            return files.filter(Files::isRegularFile).map(path -> path.getFileName().toString()).toList();
        }
    }

    @Override
    public synchronized void delete(String fileName) throws IOException {
        final Optional<Path> resolved = resolve(fileName).filter(Files::isRegularFile);
        if (resolved.isPresent())
            Files.deleteIfExists(resolved.get());
    }

    @Override
    public synchronized void deleteAll() throws IOException {
        for (String fileName : getAll()) {
            delete(fileName);
        }
    }

    /**
     * Resolves the given file name to an absolute, normalized path within the base directory of the repository.
     * If the resolved path is not within the base directory, returns an empty optional.
     *
     * @param fileName The name of the file to resolve.
     * @return An {@code Optional} containing the resolved {@code Path} if it is within the base directory,
     * or an empty {@code Optional} if it is not.
     */
    private Optional<Path> resolve(String fileName) {
        final Path resolved = baseDirectory.resolve(fileName).normalize();
        if (!resolved.startsWith(baseDirectory)) return Optional.empty();
        return Optional.of(resolved);
    }
}
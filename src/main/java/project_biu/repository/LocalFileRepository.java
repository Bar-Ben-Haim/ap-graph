package project_biu.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
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

    public LocalFileRepository(Path baseDirectory) {
        this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create storage directory: " + this.baseDirectory, e);
        }
    }

    @Override
    public synchronized void save(String fileName, String content) {
        try {
            final Path target = resolve(fileName).orElseThrow(() -> new RuntimeException("Could not resolve file: " + fileName));
            Files.createDirectories(baseDirectory);
            Files.writeString(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save file: " + fileName, e);
        }
    }

    @Override
    public synchronized boolean exists(String fileName) {
        return resolve(fileName).map(Files::exists).orElse(false);
    }

    @Override
    public synchronized Optional<Path> location(String fileName) {
        return resolve(fileName);
    }

    @Override
    public synchronized List<String> getAll() {
        if (!Files.isDirectory(baseDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(baseDirectory)) {
            return files.filter(Files::isRegularFile).map(path -> path.getFileName().toString()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list files in " + baseDirectory, e);
        }
    }

    @Override
    public synchronized void delete(String fileName) {
        try {
            final Optional<Path> resolved = resolve(fileName);
            if (resolved.isPresent())
                Files.deleteIfExists(resolved.get());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete file: " + fileName, e);
        }
    }

    @Override
    public synchronized void deleteAll() {
        getAll().forEach(this::delete);
    }

    /**
     * Resolves a name against the base directory, rejecting anything that escapes it.
     */
    private Optional<Path> resolve(String fileName) {
        final Path resolved = baseDirectory.resolve(fileName).normalize();
        if (!resolved.startsWith(baseDirectory)) return Optional.empty();
        return Optional.of(resolved);
    }
}
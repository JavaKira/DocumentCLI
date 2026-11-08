package org.example.documentCLI.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.documentCLI.application.document.DocumentRepository;
import org.example.documentCLI.domain.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//todo рефакторинг
public class FileDocumentRepository implements DocumentRepository {
    private final Path documentsDir;
    private final ObjectMapper mapper;

    public FileDocumentRepository(Path root) throws IOException {
        this.documentsDir = root.resolve("documents");
        Files.createDirectories(documentsDir);
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public List<Document> list() {
        try (var stream = Files.list(documentsDir)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(dir -> dir.resolve("document.json"))
                    .filter(Files::exists)
                    .map(this::read)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Document::date))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать список документов", e);
        }
    }

    @Override
    public Document byId(String id) {
        Path file = documentDir(id).resolve("document.json");
        if (!Files.exists(file)) throw new IllegalArgumentException("Документ \"" + id + "\" не найден.");
        return read(file);
    }

    @Override
    public boolean exists(String id) {
        return Files.exists(documentDir(id).resolve("document.json"));
    }

    @Override
    public Document update(Document d) {
        Path dir = documentDir(d.id());
        try {
            Files.createDirectories(dir);
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(dir.resolve("document.json").toFile(), d);
            return d;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить документ " + d.id(), e);
        }
    }

    @Override
    public void delete(String id) {
        Path dir = documentDir(id);
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); }
                        catch (IOException e) { throw new RuntimeException(e); }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить документ " + id, e);
        }
    }

    @Override
    public Path documentDir(String docId) {
        return documentsDir.resolve(docId);
    }

    @Override
    public Optional<Path> findScan(String docId) {
        Path dir = documentDir(docId);
        if (!Files.isDirectory(dir)) return Optional.empty();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("scan.") && !name.endsWith(".json");
                    })
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteScan(String docId) throws IOException {
        Optional<Path> scan = findScan(docId);
        if (scan.isPresent()) {
            Files.delete(scan.get());
        }
    }

    private Document read(Path p) {
        try {
            return mapper.readValue(p.toFile(), Document.class);
        } catch (IOException e) {
            System.err.println("Ошибка чтения " + p + ": " + e.getMessage());
            return null;
        }
    }
}

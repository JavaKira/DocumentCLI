package org.example.documentCLI.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.documentCLI.application.cases.CaseRepository;
import org.example.documentCLI.domain.Case;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//todo рефакторинг
public class FileCaseRepository implements CaseRepository {

    private final Path casesDir;
    private final ObjectMapper mapper;

    public FileCaseRepository(Path root) throws IOException {
        this.casesDir = root.resolve("cases");
        Files.createDirectories(casesDir);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<Case> all() {
        try (var stream = Files.list(casesDir)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(dir -> dir.resolve("case.json"))
                    .filter(Files::exists)
                    .map(this::read)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Case::id))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать список дел", e);
        }
    }

    @Override
    public Case byId(String id) {
        Path file = caseDir(id).resolve("case.json");
        if (!Files.exists(file)) throw new IllegalArgumentException("Дело " + id + " не найдено");
        return read(file);
    }

    @Override
    public boolean exists(String id) {
        return Files.exists(caseDir(id).resolve("case.json"));
    }

    @Override
    public Case update(Case c) {
        Path dir = caseDir(c.id());
        try {
            Files.createDirectories(dir);
            // updatedAt уже проставлен в момент withXxx(), но на всякий случай
            mapper.writeValue(dir.resolve("case.json").toFile(), c);
            return c;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить дело " + c.id(), e);
        }
    }

    @Override
    public void delete(String id) {
        Path dir = caseDir(id);
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); }
                        catch (IOException e) { throw new RuntimeException(e); }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить дело " + id, e);
        }
    }

    private Path caseDir(String id) {
        return casesDir.resolve(id);
    }

    private Case read(Path p) {
        try {
            return mapper.readValue(p.toFile(), Case.class);
        } catch (IOException e) {
            System.err.println("Ошибка чтения " + p + ": " + e.getMessage());
            return null;
        }
    }
}
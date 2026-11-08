package org.example.documentCLI.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.documentCLI.application.fund.FundRepository;
import org.example.documentCLI.domain.Fund;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

//todo рефакторинг
public class FileFundRepository implements FundRepository {
    private final Path fundsDir;
    private final ObjectMapper mapper;

    public FileFundRepository(Path root) throws IOException {
        this.fundsDir = root.resolve("funds");
        Files.createDirectories(fundsDir);
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public List<Fund> all() {
        try (var stream = Files.list(fundsDir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(this::read)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Fund::id))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать список фондов", e);
        }
    }

    @Override
    public Fund byId(Integer id) {
        Path file = fundsDir.resolve(id + ".json");
        if (!Files.exists(file)) throw new IllegalArgumentException("Фонд " + id + " не найден");
        return read(file);
    }

    @Override
    public boolean exists(Integer id) {
        return Files.exists(fundsDir.resolve(id + ".json"));
    }

    @Override
    public Fund update(Fund f) {
        Path file = fundsDir.resolve(f.id() + ".json");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), f);
            return f;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить фонд " + f.id(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            Files.deleteIfExists(fundsDir.resolve(id + ".json"));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить фонд " + id, e);
        }
    }

    private Fund read(Path p) {
        try {
            return mapper.readValue(p.toFile(), Fund.class);
        } catch (IOException e) {
            System.err.println("Ошибка чтения " + p + ": " + e.getMessage());
            return null;
        }
    }
}

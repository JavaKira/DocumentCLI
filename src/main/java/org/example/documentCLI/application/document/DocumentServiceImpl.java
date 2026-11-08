package org.example.documentCLI.application.document;

import org.example.documentCLI.domain.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация {@link DocumentService} поверх {@link DocumentRepository}.
 *
 * <p>Сервис отвечает за генерацию идентификаторов, валидацию входных данных
 * и оркестрацию файловых операций со сканами. Вся персистентность делегируется
 * репозиторию.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public class DocumentServiceImpl implements DocumentService {

    /** Длина усечённого UUID, используемого как id документа. */
    private static final int ID_LENGTH = 6;

    /** Базовое имя файла скана (без расширения). */
    private static final String SCAN_BASENAME = "scan";

    private final DocumentRepository documentRepository;

    /**
     * @param documentRepository репозиторий документов
     */
    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<Document> list() {
        return documentRepository.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<Document> free() {
        return list().stream()
                .filter(Document::isFree)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Document byId(String id) {
        return documentRepository
                .byId(id);
    }

    /** {@inheritDoc} */
    @Override
    public Document create(String title, LocalDate date, String note) {
        String id = generateId();
        return documentRepository.update(Document.create(id, title, date, note));
    }

    /** {@inheritDoc} */
    @Override
    public void update(Document document) {
        documentRepository.update(document);
    }

    /** {@inheritDoc} */
    @Override
    public void attachScan(String docId, Path source, boolean overwrite) throws IOException {
        byId(docId); // проверка существования документа

        validateSource(source);

        Path dir = documentRepository.documentDir(docId);
        Files.createDirectories(dir);

        Optional<Path> existing = documentRepository.findScan(docId);
        if (existing.isPresent()) {
            if (!overwrite) {
                throw new IllegalStateException(
                        "Скан уже существует: " + existing.get().getFileName()
                                + ". Используйте --overwrite для замены.");
            }
            Files.delete(existing.get());
        }

        String ext = getExtension(source.getFileName().toString());
        Path target = dir.resolve(SCAN_BASENAME + ext);
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Path> scan(String docId) {
        return documentRepository.findScan(docId);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteScan(String docId) throws IOException {
        documentRepository.deleteScan(docId);
    }

    /**
     * Генерирует новый id документа: первые {@value #ID_LENGTH} символов UUID.
     *
     * @return новый идентификатор
     */
    private String generateId() {
        return UUID.randomUUID().toString().substring(0, ID_LENGTH);
    }

    /**
     * Проверяет, что источник скана существует и является обычным файлом.
     *
     * @param source путь к исходному файлу
     * @throws IllegalArgumentException если файл не существует или не является обычным файлом
     */
    private void validateSource(Path source) {
        if (!Files.exists(source)) {
            throw new IllegalArgumentException("Файл не найден: " + source);
        }
        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException("Это не файл: " + source);
        }
    }

    /**
     * Возвращает расширение файла вместе с точкой в нижнем регистре.
     *
     * @param name имя файла
     * @return расширение (например, {@code ".pdf"}) или пустая строка, если расширения нет
     */
    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot).toLowerCase() : "";
    }
}
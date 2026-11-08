package org.example.documentCLI.application.document;

import org.example.documentCLI.domain.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий документов: абстракция над хранилищем.
 *
 * <p>Отвечает за персистентность самих записей {@link Document} и за
 * сопутствующие файловые артефакты — каталог документа и прикреплённый
 * скан. Реализации могут работать с файловой системой, БД или in-memory.</p>
 *
 * <p>Все методы, работающие с файловой системой, могут бросать unchecked
 * {@link java.io.UncheckedIOException} либо checked {@link IOException}
 * (там, где это явно объявлено).</p>
 *
 * @author Javakira
 * @since 1.0
 */
public interface DocumentRepository {

    /**
     * Возвращает все документы из хранилища.
     *
     * @return неизменяемый список документов; пустой список, если документов нет
     */
    List<Document> list();

    /**
     * Находит документ по идентификатору.
     *
     * @param id идентификатор документа (substring(0, 6) от UUID)
     * @return найденный документ
     * @throws IllegalArgumentException если документ с таким id не найден
     */
    Document byId(String id);

    /**
     * Проверяет существование документа.
     *
     * @param id идентификатор документа
     * @return {@code true}, если документ есть в хранилище
     */
    boolean exists(String id);

    /**
     * Сохраняет документ (создание или обновление).
     *
     * <p>Контракт — upsert: если документ с таким id уже есть, он
     * перезаписывается; если нет — добавляется. Возвращается актуальная
     * версия документа после сохранения.</p>
     *
     * @param d документ для сохранения
     * @return сохранённый документ
     */
    Document update(Document d);

    /**
     * Удаляет документ по идентификатору.
     *
     * <p>Если документа нет — операция должна завершиться без ошибки
     * (идемпотентное удаление).</p>
     *
     * @param id идентификатор документа
     */
    void delete(String id);

    /**
     * Возвращает путь к каталогу документа ({@code documents/<id>/}).
     *
     * <p>Метод только вычисляет путь — каталог может ещё не существовать.
     * Создание каталога — ответственность вызывающего кода (например,
     * {@code DocumentService#attachScan}).</p>
     *
     * @param docId идентификатор документа
     * @return путь к каталогу документа
     */
    Path documentDir(String docId);

    /**
     * Ищет прикреплённый скан документа.
     *
     * <p>Реализация обычно ищет файл {@code scan.*} в каталоге документа
     * и возвращает первый найденный.</p>
     *
     * @param docId идентификатор документа
     * @return {@link Optional} с путём к скану, если он есть; иначе {@link Optional#empty()}
     */
    Optional<Path> findScan(String docId);

    /**
     * Удаляет прикреплённый скан, если он есть.
     *
     * <p>Операция идемпотентна: отсутствие скана не является ошибкой.</p>
     *
     * @param docId идентификатор документа
     * @throws IOException при ошибке удаления файла
     */
    void deleteScan(String docId) throws IOException;
}
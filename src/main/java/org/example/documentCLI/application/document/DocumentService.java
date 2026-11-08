package org.example.documentCLI.application.document;

import org.example.documentCLI.domain.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой для работы с документами делопроизводства.
 *
 * <p>Предоставляет операции чтения, создания, обновления документов, а также
 * управления прикреплёнными сканами (файлами) на диске. Реализации могут
 * использовать файловое хранилище, БД или in-memory структуры.</p>
 *
 * <p>Методы сервиса не бросают unchecked-исключений «напрямую» — ошибки
 * ввода-вывода и некорректные состояния транслируются в
 * {@link IllegalArgumentException} (некорректный аргумент) или
 * {@link IllegalStateException} (нарушение состояния хранилища). Там, где
 * отсутствие результата является нормальным сценарием, возвращается
 * {@link Optional#empty()}.</p>
 *
 * <p>Исключение — операции со сканами ({@link #attachScan}, {@link #deleteScan}):
 * они объявляют checked {@link IOException}, поскольку работают напрямую с
 * файловой системой, и вызывающий код (CLI-команды) обязан обработать
 * возможные сбои ввода-вывода.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public interface DocumentService {

    /**
     * Возвращает все документы.
     *
     * @return неизменяемый список документов; пустой список, если документов нет
     */
    List<Document> list();

    /**
     * Возвращает только «свободные» документы, не привязанные ни к какому делу
     * (у которых {@code caseId == null}).
     *
     * @return неизменяемый список свободных документов; пустой список, если таких нет
     */
    List<Document> free();

    /**
     * Находит документ по его идентификатору.
     *
     * @param id идентификатор документа (substring(0, 6) от UUID)
     * @return найденный документ
     * @throws IllegalArgumentException если документ с таким id не найден
     */
    Document byId(String id);

    /**
     * Создаёт новый документ.
     *
     * <p>Документ создаётся «свободным» ({@code caseId == null}) — привязка к
     * делу выполняется отдельно через {@code CaseService} / {@code linkTo}.
     * Регистрационный номер ({@code regNumber}) на этом этапе не присваивается:
     * он выдаётся делом в момент привязки.</p>
     *
     * @param title заголовок документа
     * @param date  дата документа; если {@code null} — текущая дата
     * @param note  примечание (копия, оригинал, срок действия и т.п.)
     * @return созданный документ
     * @throws IllegalArgumentException если {@code title} пуст
     */
    Document create(String title, LocalDate date, String note);

    /**
     * Сохраняет изменения существующего документа.
     *
     * <p>Обычно вызывается после {@link Document#withTitle}, {@link Document#withNote}
     * и других {@code with*}-методов. Документ должен существовать в хранилище —
     * создание через {@code update} не допускается (для этого есть
     * {@link #create}).</p>
     *
     * @param document документ с обновлёнными полями
     * @throws IllegalArgumentException если {@code document == null}
     * @throws IllegalStateException    если документ с таким id не найден
     */
    void update(Document document);

    /**
     * Прикрепляет скан (файл) к документу.
     *
     * <p>Файл копируется в каталог документа под именем {@code scan.<ext>},
     * где {@code <ext>} — расширение исходного файла в нижнем регистре. Если
     * скан уже существует и {@code overwrite == false} — операция завершается
     * ошибкой {@link IllegalStateException}.</p>
     *
     * @param docId     идентификатор документа
     * @param source    путь к исходному файлу скана
     * @param overwrite перезаписать существующий скан
     * @throws IllegalArgumentException если документ не найден, {@code source} не существует
     *                                  или не является обычным файлом
     * @throws IllegalStateException    если скан уже есть, а {@code overwrite == false}
     * @throws IOException              при ошибке копирования файла или создания каталога
     */
    void attachScan(String docId, Path source, boolean overwrite) throws IOException;

    /**
     * Возвращает путь к прикреплённому скану документа.
     *
     * @param docId идентификатор документа
     * @return {@link Optional} с путём к скану, если он прикреплён; иначе {@link Optional#empty()}
     */
    Optional<Path> scan(String docId);

    /**
     * Удаляет прикреплённый скан документа.
     *
     * <p>Операция идемпотентна: если скан отсутствует — завершается без ошибки.</p>
     *
     * @param docId идентификатор документа
     * @throws IOException при ошибке удаления файла
     */
    void deleteScan(String docId) throws IOException;
}
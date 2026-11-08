package org.example.documentCLI.application.cases;

import org.example.documentCLI.domain.Case;
import org.example.documentCLI.domain.Document;

import java.time.LocalDate;
import java.util.List;

/**
 * Сервисный слой для работы с делами делопроизводства.
 *
 * <p>Отвечает за жизненный цикл дел (создание, обновление, удаление),
 * управление привязкой документов к делам, а также за поддержку
 * регистрационной нумерации документов внутри дела.</p>
 *
 * <p>Регистрационный номер ({@code regNumber}) выдаётся делом в момент
 * привязки документа и сохраняется за документом даже после отвязки —
 * как историческое значение. Повторное использование номера в рамках
 * одного дела не допускается.</p>
 *
 * <p>Методы сервиса не бросают checked-исключений. Некорректные аргументы
 * транслируются в {@link IllegalArgumentException}, нарушения состояния
 * хранилища — в {@link IllegalStateException}. Там, где отсутствие
 * результата — нормальный сценарий, возвращается {@code null} (для
 * одиночных объектов) или пустой список.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public interface CaseService {

    /**
     * Возвращает все дела.
     *
     * @return неизменяемый список дел; пустой список, если дел нет
     */
    List<Case> all();

    /**
     * Находит дело по идентификатору.
     *
     * @param id идентификатор дела (например, {@code 0001-2026}, {@code ВУ}, {@code ПСИ-1})
     * @return найденное дело
     * @throws IllegalArgumentException если дело с таким id не найдено
     */
    Case byId(String id);

    /**
     * Проверяет существование дела.
     *
     * @param id идентификатор дела
     * @return {@code true}, если дело есть в хранилище
     */
    boolean exists(String id);

    /**
     * Возвращает документы дела, отсортированные по {@code regNumber}.
     *
     * <p>Если среди {@code documentIDs} дела остались ссылки на удалённые
     * документы, они молча пропускаются — метод не падает.</p>
     *
     * @param id идентификатор дела
     * @return список документов дела; пустой список, если документов нет
     * @throws IllegalArgumentException если дело с таким id не найдено
     */
    List<Document> documentsOf(String id);

    /**
     * Возвращает дела указанного фонда.
     *
     * @param id идентификатор фонда
     * @return список дел фонда; пустой список, если дел нет
     */
    List<Case> byFund(Integer id);

    /**
     * Создаёт новое дело в фонде.
     *
     * <p>Дело создаётся открытым ({@code closedAt == null}), с
     * {@code nextRegNum == 1} и пустым списком документов.</p>
     *
     * @param fundId   идентификатор фонда
     * @param id       идентификатор дела (например, {@code 0001-2026})
     * @param title    заголовок дела
     * @param openedAt дата открытия дела; если {@code null} — текущая дата
     * @return созданное дело
     * @throws IllegalArgumentException если {@code fundId} или {@code id} некорректны,
     *                                  либо дело с таким id уже существует
     */
    Case create(Integer fundId, String id, String title, LocalDate openedAt);

    /**
     * Сохраняет изменения существующего дела.
     *
     * <p>Обычно вызывается после {@code with*}-методов {@link Case}.
     * Дело должно существовать в хранилище.</p>
     *
     * @param c дело с обновлёнными полями
     * @throws IllegalArgumentException если {@code c == null}
     * @throws IllegalStateException    если дело с таким id не найдено
     */
    void update(Case c);

    /**
     * Удаляет дело.
     *
     * <p>Документы удалённого дела переходят в «свободное плавание»:
     * теряют {@code caseId}, но сохраняют {@code regNumber} как
     * историческое значение.</p>
     *
     * @param id идентификатор дела
     * @return количество освобождённых документов
     * @throws IllegalArgumentException если дело с таким id не найдено
     */
    int delete(String id);

    /**
     * Привязывает документ к делу, присваивая ему новый {@code regNumber}.
     *
     * <p>Документ должен быть свободен ({@code caseId == null}). Номер
     * берётся из {@code nextRegNum} дела и инкрементирует его. Если документ
     * уже привязан к другому делу — операция завершается ошибкой.</p>
     *
     * @param caseId идентификатор дела
     * @param docId  идентификатор документа
     * @throws IllegalArgumentException если дело или документ не найдены
     * @throws IllegalStateException    если документ уже привязан к делу
     */
    void linkDocument(String caseId, String docId);

    /**
     * Отвязывает документ от дела.
     *
     * <p>{@code regNumber} сохраняется за документом как историческое
     * значение. Если документ не привязан ни к какому делу — операция
     * завершается без ошибки (идемпотентна).</p>
     *
     * @param docId идентификатор документа
     * @throws IllegalArgumentException если документ не найден
     */
    void unlinkDocument(String docId);

    /**
     * Возвращает следующий свободный {@code regNumber} в деле без его
     * резервирования.
     *
     * <p>Используется для предпросмотра — например, чтобы показать
     * пользователю «документ получит №5» до фактической привязки.
     * Фактическая выдача номера происходит в {@link #linkDocument}.</p>
     *
     * @param caseId идентификатор дела
     * @return следующий номер, который будет присвоен документу
     * @throws IllegalArgumentException если дело с таким id не найдено
     */
    int peekNextRegNumber(String caseId);

    /**
     * Возвращает количество дел в фонде.
     *
     * @param fundId идентификатор фонда
     * @return количество дел; {@code 0}, если дел нет
     */
    int countByFund(Integer fundId);
}
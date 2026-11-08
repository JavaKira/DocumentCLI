package org.example.documentCLI.application.fund;

import org.example.documentCLI.domain.Fund;

import java.util.List;

/**
 * Сервисный слой для работы с фондами делопроизводства.
 *
 * <p>Отвечает за жизненный цикл фондов: создание с автоматической выдачей
 * идентификатора, чтение, обновление и удаление. Вся персистентность
 * делегируется {@link FundRepository}.</p>
 *
 * <p>Идентификатор фонда ({@code Integer}) генерируется сервисом через
 * {@link #nextId()} — это следующий свободный номер после максимального
 * существующего.</p>
 *
 * <p>Методы сервиса не бросают checked-исключений. Некорректные аргументы
 * транслируются в {@link IllegalArgumentException}, нарушения состояния
 * хранилища — в {@link IllegalStateException}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public interface FundService {

    /**
     * Создаёт новый фонд.
     *
     * <p>Идентификатор фонда генерируется автоматически через
     * {@link #nextId()}. Если фонд с таким id уже существует (например,
     * из-за гонки), операция завершается ошибкой.</p>
     *
     * @param title название фонда
     * @param note  примечание к фонду
     * @return созданный фонд
     * @throws IllegalArgumentException если {@code title} пуст
     */
    Fund create(String title, String note);

    /**
     * Возвращает следующий свободный идентификатор фонда.
     *
     * <p>Не резервирует номер: между вызовом {@code nextId()} и фактическим
     * сохранением фонда другой вызов может получить тот же id. Для
     * конкурентных сценариев используйте {@link #create}.</p>
     *
     * @return следующий свободный id (максимальный существующий + 1, либо {@code 1})
     */
    Integer nextId();

    /**
     * Возвращает все фонды.
     *
     * @return неизменяемый список фондов; пустой список, если фондов нет
     */
    List<Fund> all();

    /**
     * Проверяет существование фонда.
     *
     * @param id идентификатор фонда
     * @return {@code true}, если фонд есть в хранилище
     */
    boolean exist(Integer id);

    /**
     * Находит фонд по идентификатору.
     *
     * @param id идентификатор фонда
     * @return найденный фонд
     * @throws IllegalArgumentException если фонд с таким id не найден
     */
    Fund byId(Integer id);

    /**
     * Сохраняет изменения существующего фонда.
     *
     * <p>Обычно вызывается после {@code with*}-методов {@link Fund}.
     * Создание через {@code update} не допускается — для этого есть
     * {@link #create}.</p>
     *
     * @param fund фонд с обновлёнными полями
     * @throws IllegalArgumentException если {@code fund == null}
     * @throws IllegalStateException    если фонд с таким id не найден
     */
    void update(Fund fund);

    /**
     * Удаляет фонд по идентификатору.
     *
     * <p>Операция идемпотентна: если фонда нет — завершается без ошибки.
     * Реализация может дополнительно проверять, что в фонде нет дел,
     * и бросать {@link IllegalStateException} в этом случае.</p>
     *
     * @param id идентификатор фонда
     * @throws IllegalStateException если удаление запрещено (например, в фонде есть дела)
     */
    void delete(Integer id);
}
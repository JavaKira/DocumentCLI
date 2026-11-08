package org.example.documentCLI.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Запись, описывающая дело делопроизводства.
 *
 * @param id          ID дела в формате {@code 0001-2026} (для временных дел) и {@code ВУ, ПСИ-1, ПСИ-2} (для постоянных дел)
 * @param fundId      id фонда дела
 * @param title       заголовок дела, описание содержимого
 * @param startedAt   дата начала дела (первый документ)
 * @param closedAt    дата закрытия дела (последний документ). {@code null}, если дело не закрыто
 * @param nextRegNum  регистрационный индекс следующего документа, который будет добавлен в дело
 * @param documentIDs документы в деле
 * @author Javakira
 * @since 1.0
 */
public record Case(
        @JsonProperty("id")          String id,
        @JsonProperty("fundId")      Integer fundId,
        @JsonProperty("title")       String title,
        @JsonProperty("startedAt")   LocalDate startedAt,
        @JsonProperty("closedAt")    LocalDate closedAt,
        @JsonProperty("nextRegNum")  int nextRegNum,
        @JsonProperty("documentIDs") List<String> documentIDs
) {

    /**
     * Канонический конструктор с поддержкой десериализации Jackson.
     * Нормализует список документов: {@code null} → пустой неизменяемый список.
     */
    @JsonCreator
    public Case {
        if (documentIDs == null) {
            documentIDs = List.of();
        } else {
            documentIDs = List.copyOf(documentIDs);
        }
    }

    /**
     * Создаёт новое (открытое) дело.
     *
     * @param id        идентификатор дела
     * @param fundId    id фонда
     * @param title     заголовок дела
     * @param startedAt дата начала дела; если {@code null} — текущая дата
     * @return новый экземпляр Case
     */
    public static Case create(String id, Integer fundId, String title, LocalDate startedAt) {
        return new Case(
                id,
                fundId,
                title,
                startedAt != null ? startedAt : LocalDate.now(),
                null,
                1,
                List.of()
        );
    }

    /**
     * Создаёт копию дела с новым заголовком.
     *
     * @param newTitle новый заголовок
     * @return новый экземпляр Case
     */
    public Case withTitle(String newTitle) {
        return new Case(id, fundId, newTitle, startedAt, closedAt, nextRegNum, documentIDs);
    }

    /**
     * Создаёт копию дела с новым id фонда.
     *
     * @param newFundId новый id фонда
     * @return новый экземпляр Case
     */
    public Case withFundId(Integer newFundId) {
        return new Case(id, newFundId, title, startedAt, closedAt, nextRegNum, documentIDs);
    }

    /**
     * Создаёт копию дела с новым значением nextRegNum.
     *
     * @param newValue новое значение регистрационного индекса
     * @return новый экземпляр Case
     */
    public Case withNextRegNum(int newValue) {
        return new Case(id, fundId, title, startedAt, closedAt, newValue, documentIDs);
    }

    /**
     * Создаёт копию дела с новой датой начала.
     *
     * @param newStartedAt новая дата начала
     * @return новый экземпляр Case
     */
    public Case withStartedAt(LocalDate newStartedAt) {
        return new Case(id, fundId, title, newStartedAt, closedAt, nextRegNum, documentIDs);
    }

    /**
     * Создаёт копию дела с новой датой закрытия.
     *
     * @param newClosedAt новая дата закрытия ({@code null} — дело открыто)
     * @return новый экземпляр Case
     */
    public Case withClosedAt(LocalDate newClosedAt) {
        return new Case(id, fundId, title, startedAt, newClosedAt, nextRegNum, documentIDs);
    }

    /**
     * Закрывает дело: устанавливает {@code closedAt = today}.
     *
     * @return новый экземпляр Case с проставленной датой закрытия
     */
    public Case close() {
        return withClosedAt(LocalDate.now());
    }

    /**
     * Открывает дело заново: сбрасывает {@code closedAt} в {@code null}.
     *
     * @return новый экземпляр Case без даты закрытия
     */
    public Case reopen() {
        return withClosedAt(null);
    }

    /**
     * Забирает следующий регистрационный номер и возвращает обновлённое дело.
     *
     * @return пара «выданный номер + обновлённое дело»
     */
    public TakeRegNum takeNextRegNum() {
        return new TakeRegNum(nextRegNum, withNextRegNum(nextRegNum + 1));
    }

    /**
     * Результат выдачи очередного регистрационного номера.
     *
     * @param regNumber выданный номер
     * @param updated   обновлённое дело с инкрементированным {@code nextRegNum}
     */
    public record TakeRegNum(int regNumber, Case updated) {}

    /**
     * Создаёт копию дела с добавленным документом.
     *
     * @param docId идентификатор документа
     * @return новый экземпляр Case
     */
    public Case withAddedDocument(String docId) {
        var copy = new ArrayList<>(documentIDs);
        copy.add(docId);
        return new Case(id, fundId, title, startedAt, closedAt, nextRegNum, List.copyOf(copy));
    }

    /**
     * Создаёт копию дела с удалённым документом.
     *
     * @param docId идентификатор документа
     * @return новый экземпляр Case
     */
    public Case withRemovedDocument(String docId) {
        var copy = new ArrayList<>(documentIDs);
        copy.remove(docId);
        return new Case(id, fundId, title, startedAt, closedAt, nextRegNum, List.copyOf(copy));
    }

    /**
     * @return количество документов в деле
     */
    public int documentsCount() {
        return documentIDs.size();
    }

    /**
     * @return {@code true}, если дело закрыто
     */
    @JsonIgnore
    public boolean isClosed() {
        return closedAt != null;
    }

    /**
     * @return {@code true}, если дело открыто
     */
    @JsonIgnore
    public boolean isOpen() {
        return closedAt == null;
    }
}
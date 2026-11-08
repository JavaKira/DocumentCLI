package org.example.documentCLI.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Запись, описывающая документ делопроизводства.
 *
 * @param id        substring(0, 6) от UUID. ID документа внутри системы (не имеет физического представления)
 * @param regNumber регистрационный номер документа. Н-р 0001-2026/0001 -> дело 0001-2026 документ 1.
 *                  Присваивается при присвоении документа к делу, не удаляется после отвязки
 * @param title     заголовок документа
 * @param date      дата документа
 * @param note      примечание к документу (копия, оригинал, действителен до xx.xx.xxxx)
 * @param caseId    идентификатор дела, к которому относится документ. Если null, то документ "свободный"
 * @author Javakira
 * @since 1.0
 */
public record Document(
        @JsonProperty("id") String id,
        @JsonProperty("regNumber") Integer regNumber,
        @JsonProperty("title") String title,
        @JsonProperty("date") LocalDate date,
        @JsonProperty("note") String note,
        @JsonProperty("caseId") String caseId
) {
    /**
     * Создаёт копию записи с новым значением id.
     *
     * @param id новый идентификатор
     * @return новый экземпляр Document
     */
    public Document withId(String id) {
        return new Document(id, this.regNumber, this.title, this.date, this.note, this.caseId);
    }

    /**
     * Создаёт копию записи с новым значением regNumber.
     *
     * @param regNumber новый регистрационный номер
     * @return новый экземпляр Document
     */
    public Document withRegNumber(Integer regNumber) {
        return new Document(this.id, regNumber, this.title, this.date, this.note, this.caseId);
    }

    /**
     * Создаёт копию записи с новым значением title.
     *
     * @param title новый заголовок
     * @return новый экземпляр Document
     */
    public Document withTitle(String title) {
        return new Document(this.id, this.regNumber, title, this.date, this.note, this.caseId);
    }

    /**
     * Создаёт копию записи с новым значением date.
     *
     * @param date новая дата
     * @return новый экземпляр Document
     */
    public Document withDate(LocalDate date) {
        return new Document(this.id, this.regNumber, this.title, date, this.note, this.caseId);
    }

    /**
     * Создаёт копию записи с новым значением note.
     *
     * @param note новое примечание
     * @return новый экземпляр Document
     */
    public Document withNote(String note) {
        return new Document(this.id, this.regNumber, this.title, this.date, note, this.caseId);
    }

    /**
     * Создаёт копию записи с новым значением caseId.
     *
     * @param caseId новый идентификатор дела
     * @return новый экземпляр Document
     */
    public Document withCaseId(String caseId) {
        return new Document(this.id, this.regNumber, this.title, this.date, this.note, caseId);
    }

    /**
     * Привязка к делу: присваивается новый regNumber.
     * regNumber передаётся снаружи — его выдаёт Case через takeNextRegNum().
     *
     * @param newCaseId    идентификатор дела
     * @param newRegNumber новый регистрационный номер
     * @return новый экземпляр Document, привязанный к делу
     */
    public Document linkTo(String newCaseId, int newRegNumber) {
        if (newCaseId == null) {
            throw new IllegalArgumentException("caseId не может быть null");
        }
        return new Document(id, newRegNumber, title, date, note, newCaseId);
    }

    /**
     * Отвязка от дела. regNumber сохраняется как историческое значение.
     *
     * @return новый экземпляр Document без привязки к делу
     */
    public Document unlink() {
        return new Document(id, regNumber, title, date, note, null);
    }

    /** Документ не привязан ни к какому делу. */
    @JsonIgnore
    public boolean isFree() {
        return caseId == null;
    }

    /** Документ привязан к делу. */
    @JsonIgnore
    public boolean isAttached() {
        return caseId != null;
    }

    /**
     * Новый свободный документ (без привязки к делу).
     *
     * @param id    идентификатор документа
     * @param title заголовок
     * @param date  дата документа
     * @param note  примечание
     * @return новый экземпляр Document
     */
    public static Document create(String id, String title, LocalDate date, String note) {
        return new Document(id, null, title, date, note, null);
    }
}
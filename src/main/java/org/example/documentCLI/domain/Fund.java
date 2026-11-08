package org.example.documentCLI.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Запись, описывающая фонд делопроизводства
 *
 * @param id    уникальный идентификатор фонда. Автоинкриментируется при создании нового фонда
 * @param title название фонда. Н-р "личный архив В. В. А."
 * @param note  примечание к фонду. Н-р какие дела к нему относятся
 * @author Javakira
 * @since 1.0
 */
public record Fund(
        @JsonProperty("id") Integer id,
        @JsonProperty("title") String title,
        @JsonProperty("note") String note
) {
    /**
     * Создаёт копию записи с новым значением id
     * @param id новый идентификатор
     * @return новый экземпляр Fund
     */
    public Fund withId(Integer id) {
        return new Fund(id, this.title, this.note);
    }

    /**
     * Создаёт копию записи с новым значением title
     * @param title новое название
     * @return новый экземпляр Fund
     */
    public Fund withTitle(String title) {
        return new Fund(this.id, title, this.note);
    }

    /**
     * Создаёт копию записи с новым значением note
     * @param note новое примечание
     * @return новый экземпляр Fund
     */
    public Fund withNote(String note) {
        return new Fund(this.id, this.title, note);
    }
}

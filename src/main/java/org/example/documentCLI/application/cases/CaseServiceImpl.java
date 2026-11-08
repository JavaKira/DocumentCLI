package org.example.documentCLI.application.cases;

import org.example.documentCLI.application.document.DocumentRepository;
import org.example.documentCLI.application.fund.FundRepository;
import org.example.documentCLI.domain.Case;
import org.example.documentCLI.domain.Document;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Реализация {@link CaseService} поверх {@link CaseRepository},
 * {@link DocumentRepository} и {@link FundRepository}.
 *
 * <p>Сервис отвечает за жизненный цикл дел и за согласованное изменение
 * связанных сущностей: при привязке/отвязке документа обновляются и дело,
 * и сам документ; при удалении дела его документы «освобождаются».</p>
 *
 * <p>Поскольку {@link Case} и {@link Document} — неизменяемые record'ы,
 * любые изменения выполняются через {@code with*}-методы с последующим
 * сохранением через репозиторий.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public class CaseServiceImpl implements CaseService {

    /** Регулярное выражение допустимых идентификаторов дела. */
    private static final String ID_PATTERN = "[A-Za-zА-Яа-я0-9_\\-]+";

    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final FundRepository fundRepository;

    /**
     * @param caseRepository     репозиторий дел
     * @param documentRepository репозиторий документов
     * @param fundRepository     репозиторий фондов
     */
    public CaseServiceImpl(CaseRepository caseRepository,
                           DocumentRepository documentRepository,
                           FundRepository fundRepository) {
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
        this.fundRepository = fundRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<Case> all() {
        return caseRepository.all();
    }

    /** {@inheritDoc} */
    @Override
    public Case byId(String id) {
        return caseRepository.byId(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(String id) {
        return caseRepository.exists(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<Document> documentsOf(String id) {
        return byId(id).documentIDs().stream()
                .map(documentRepository::byId)
                .sorted(Comparator.comparing(
                        Document::regNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<Case> byFund(Integer id) {
        return caseRepository.all().stream()
                .filter(c -> id.equals(c.fundId()))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Case create(Integer fundId, String id, String title, LocalDate openedAt) {
        validateId(id);
        validateFundExists(fundId);
        validateCaseNotExists(id);

        Case c = Case.create(id, fundId, title, openedAt);
        caseRepository.update(c);
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public void update(Case c) {
        caseRepository.update(c);
    }

    /** {@inheritDoc} */
    @Override
    public int delete(String id) {
        Case c = caseRepository.byId(id);

        int freed = 0;
        for (String docId : c.documentIDs()) {
            Document d = documentRepository.byId(docId);
            documentRepository.update(d.withCaseId(null));
            freed++;
        }

        caseRepository.delete(id);
        return freed;
    }

    /** {@inheritDoc} */
    @Override
    public void linkDocument(String caseId, String docId) {
        Case c = caseRepository.byId(caseId);
        Document d = documentRepository.byId(docId);

        if (d.caseId() != null) {
            throw new IllegalStateException(
                    "Документ уже привязан к делу \"" + d.caseId() + "\".");
        }
        if (c.documentIDs().contains(docId)) {
            throw new IllegalStateException(
                    "Документ уже числится в деле \"" + caseId + "\".");
        }

        int num = c.takeNextRegNum().regNumber();
        documentRepository.update(d.withRegNumber(num).withCaseId(caseId));
        caseRepository.update(c.withAddedDocument(docId).withNextRegNum(num + 1));
    }

    /** {@inheritDoc} */
    @Override
    public void unlinkDocument(String docId) {
        Document d = documentRepository.byId(docId);

        if (d.caseId() == null) {
            throw new IllegalStateException("Документ уже свободен.");
        }

        Case c = caseRepository.byId(d.caseId());
        caseRepository.update(c.withRemovedDocument(docId));
        documentRepository.update(d.withCaseId(null));
    }

    /** {@inheritDoc} */
    @Override
    public int peekNextRegNumber(String caseId) {
        return caseRepository.byId(caseId).nextRegNum();
    }

    /** {@inheritDoc} */
    @Override
    public int countByFund(Integer fundId) {
        return Math.toIntExact(caseRepository.all().stream()
                .filter(c -> fundId.equals(c.fundId()))
                .count());
    }

    // ---- Внутренние помощники ----

    /**
     * Проверяет корректность идентификатора дела.
     *
     * @param id идентификатор дела
     * @throws IllegalArgumentException если id пуст или содержит недопустимые символы
     */
    private void validateId(String id) {
        if (id == null || !id.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "ID дела может содержать только буквы, цифры, _ и -");
        }
    }

    /**
     * Проверяет существование фонда.
     *
     * @param fundId идентификатор фонда
     * @throws IllegalArgumentException если фонд не найден
     */
    private void validateFundExists(Integer fundId) {
        if (!fundRepository.exists(fundId)) {
            throw new IllegalArgumentException(
                    "Фонд \"" + fundId + "\" не найден. Создайте: fund mk --title \"...\"");
        }
    }

    /**
     * Проверяет, что дело с таким id ещё не существует.
     *
     * @param id идентификатор дела
     * @throws IllegalArgumentException если дело уже существует
     */
    private void validateCaseNotExists(String id) {
        if (caseRepository.exists(id)) {
            throw new IllegalArgumentException("Дело \"" + id + "\" уже существует.");
        }
    }
}
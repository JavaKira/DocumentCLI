package org.example.documentCLI.cli.cases;

import org.example.documentCLI.application.cases.CaseService;
import org.example.documentCLI.application.document.DocumentService;
import org.example.documentCLI.application.fund.FundService;
import org.example.documentCLI.cli.Console;
import org.example.documentCLI.domain.Case;
import org.example.documentCLI.domain.Document;
import picocli.CommandLine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Подкоманда {@code case ls}: выводит список дел или содержимое одного дела.
 *
 * <p>Без аргументов печатает все дела, сгруппированные по фондам. Если указан
 * позиционный аргумент — печатает карточку дела и список его документов
 * (с отметкой {@code [скан]} у тех, к которым прикреплён файл).</p>
 *
 * <p>Опция {@code --fund} ограничивает список дел одним фондом. Она
 * игнорируется, если указан id конкретного дела.</p>
 *
 * @author Javakira
 * @since 1.0
 */
@CommandLine.Command(
        name = "ls",
        description = "Список дел или содержимое одного дела"
)
public class CaseLs implements Callable<Integer> {

    /** Код возврата при успехе. */
    private static final int EXIT_OK = 0;

    /** Ширина колонки id дела при форматировании. */
    private static final int ID_WIDTH = 12;

    /** Ширина колонки заголовка дела при форматировании. */
    private static final int TITLE_WIDTH = 32;

    /** Ширина колонки заголовка документа при форматировании. */
    private static final int DOC_TITLE_WIDTH = 30;

    private final CaseService caseService;
    private final FundService fundService;
    private final DocumentService docService;

    /**
     * @param caseService сервис дел
     * @param fundService сервис фондов
     * @param docService  сервис документов
     */
    public CaseLs(CaseService caseService,
                  FundService fundService,
                  DocumentService docService) {
        this.caseService = caseService;
        this.fundService = fundService;
        this.docService = docService;
    }

    @CommandLine.Parameters(
            index = "0",
            arity = "0..1",
            description = "ID дела (если не указан — список всех)"
    )
    private String caseId;

    @CommandLine.Option(
            names = "--fund",
            description = "Фильтр по фонду"
    )
    private String fundFilter;

    /**
     * Выполняет команду.
     *
     * @return {@code 0} при успехе
     */
    @Override
    public Integer call() {
        return caseId != null ? showCase(caseId) : listCases();
    }

    /**
     * Печатает список дел, сгруппированный по фондам.
     *
     * @return {@code 0} при успехе
     */
    private int listCases() {
        List<Case> cases = caseService.all();
        if (fundFilter != null) {
            cases = cases.stream()
                    .filter(c -> fundFilter.equals(String.valueOf(c.fundId())))
                    .toList();
        }
        if (cases.isEmpty()) {
            Console.print("Дел нет.");
            return EXIT_OK;
        }

        Map<Integer, List<Case>> byFund = cases.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Case::fundId,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));

        for (Map.Entry<Integer, List<Case>> entry : byFund.entrySet()) {
            Console.print("Фонд: " + fundService.byId(entry.getKey()).title());
            for (Case c : entry.getValue()) {
                Console.print(String.format("  %-" + ID_WIDTH + "s %-" + TITLE_WIDTH + "s %d док.",
                        c.id(),
                        c.title(),
                        c.documentIDs().size()));
            }
            Console.print("");
        }
        return EXIT_OK;
    }

    /**
     * Печатает карточку одного дела и список его документов.
     *
     * @param id идентификатор дела
     * @return {@code 0} при успехе
     * @throws IllegalArgumentException если дело с таким id не найдено
     */
    private int showCase(String id) {
        Case c = caseService.byId(id);
        String fundTitle = fundService.byId(c.fundId()).title();

        Console.print("Дело " + c.id() + " — " + c.title());
        Console.print("Фонд: " + fundTitle);
        Console.print("Начато: " + c.startedAt());
        Console.print("");

        List<Document> docs = caseService.documentsOf(c.id());
        Console.print("Документы (" + docs.size() + "):");
        if (docs.isEmpty()) {
            Console.print("  —");
        } else {
            for (Document d : docs) {
                Console.print(formatDocument(d, c));
            }
        }
        Console.print("");
        return EXIT_OK;
    }

    /**
     * Форматирует строку документа для вывода в карточке дела.
     *
     * @param d документ
     * @return строка вида {@code "  1. 2026-01-01  Заголовок  — примечание  [скан]"}
     */
    private String formatDocument(Document d, Case c) {
        String note = d.note() != null ? "  — " + d.note() : "";
        String scan = docService.scan(d.id()).isPresent() ? "  [скан]" : "";
        return String.format("  %s %s  %-" + DOC_TITLE_WIDTH + "s%s%s",
                c.id() + "/" + String.format("%04d", d.regNumber()),
                d.date(),
                d.title(),
                note,
                scan);
    }
}
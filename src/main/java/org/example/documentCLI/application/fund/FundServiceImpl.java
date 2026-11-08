package org.example.documentCLI.application.fund;

import org.example.documentCLI.domain.Fund;

import java.util.List;

/**
 * Реализация {@link FundService} поверх {@link FundRepository}.
 *
 * <p>Сервис отвечает за генерацию идентификаторов и оркестрацию операций
 * с фондами. Вся персистентность делегируется репозиторию.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public class FundServiceImpl implements FundService {

    private final FundRepository fundRepo;

    /**
     * @param fundRepo репозиторий фондов
     */
    public FundServiceImpl(FundRepository fundRepo) {
        this.fundRepo = fundRepo;
    }

    /** {@inheritDoc} */
    @Override
    public Fund create(String title, String note) {
        int id = nextId();
        Fund fund = new Fund(id, title, note);
        fundRepo.update(fund);
        return fund;
    }

    /** {@inheritDoc} */
    @Override
    public Integer nextId() {
        return all().stream()
                .map(Fund::id)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    /** {@inheritDoc} */
    @Override
    public List<Fund> all() {
        return fundRepo.all();
    }

    /** {@inheritDoc} */
    @Override
    public boolean exist(Integer id) {
        return fundRepo.exists(id);
    }

    /** {@inheritDoc} */
    @Override
    public Fund byId(Integer id) {
        return fundRepo.byId(id);
    }

    /** {@inheritDoc} */
    @Override
    public void update(Fund fund) {
        fundRepo.update(fund);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(Integer id) {
        fundRepo.delete(id);
    }
}
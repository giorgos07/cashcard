package eu.quento.cashcard.data.repositories;

import eu.quento.cashcard.data.models.DbCashCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CashCardRepository extends CrudRepository<DbCashCard, Long>, PagingAndSortingRepository<DbCashCard, Long> {
    boolean existsByIdAndOwner(Long cashCardId, String owner);
    DbCashCard findByIdAndOwner(long id, String owner);
    Page<DbCashCard> findAllByOwner(String owner, PageRequest pageRequest);
}

package eu.quento.cashcard.controllers;

import eu.quento.cashcard.data.models.DbCashCard;
import eu.quento.cashcard.data.repositories.CashCardRepository;
import eu.quento.cashcard.models.requests.CreateCashCardRequest;
import eu.quento.cashcard.models.requests.UpdateCashCardRequest;
import eu.quento.cashcard.models.responses.CashCardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cash-cards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping
    private ResponseEntity<List<CashCardResponse>> getList(Pageable pageable, Principal principal) {
        Page<DbCashCard> page = cashCardRepository.findAllByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );

        var result = page
                .getContent()
                .stream()
                .map(x -> new CashCardResponse(x.id(), x.amount()))
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping
    private ResponseEntity<Void> create(
            @RequestBody CreateCashCardRequest createCashCardRequest,
            UriComponentsBuilder ucb,
            Principal principal) {

        DbCashCard savedCashCard = cashCardRepository.save(new DbCashCard(null, createCashCardRequest.amount(), principal.getName()));

        URI locationOfNewCashCard = ucb
                .path("cash-cards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();

        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping("/{id}")
    private ResponseEntity<CashCardResponse> getById(@PathVariable Long id, Principal principal) {
        Optional<DbCashCard> dbCashCard = Optional.ofNullable(cashCardRepository.findByIdAndOwner(id, principal.getName()));

        if (dbCashCard.isPresent()) {
            var response = new CashCardResponse(dbCashCard.get().id(), dbCashCard.get().amount());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    private ResponseEntity<Void> update(@PathVariable Long id, @RequestBody UpdateCashCardRequest updateCashCardRequest, Principal principal) {
        DbCashCard cashCard = cashCardRepository.findByIdAndOwner(id, principal.getName());

        if (cashCard == null) {
            return ResponseEntity.notFound().build();
        }

        var updatedCashCard = new DbCashCard(cashCard.id(), updateCashCardRequest.amount(), principal.getName());
        cashCardRepository.save(updatedCashCard);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (!cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            return ResponseEntity.notFound().build();
        }

        cashCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

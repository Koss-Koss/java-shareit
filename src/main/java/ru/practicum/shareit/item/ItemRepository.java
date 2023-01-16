package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    default Item extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующую вещь с id = " + id));
    }

    Page<Item> findAllByOwnerId(long ownerId, Pageable pageable);

    Optional<Item> findFirstByOwnerId(long ownerId);

    @Query("select i from Item i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            " and i.available = true")
    Page<Item> findAvailableByText(String text, Pageable pageable);

    Collection<Item> findAllByRequestId(long requestId);
}

package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {

    default Item extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующую вещь с id = " + id));
    }

    @Query("select i from Item i where i.owner.id = ?1 order by i.id")
    Collection<Item> findAllByOwnerId(long ownerId);

    @Query("select i from Item i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            " and i.available = true")
    Collection<Item> findAvailableByText(String text);
}
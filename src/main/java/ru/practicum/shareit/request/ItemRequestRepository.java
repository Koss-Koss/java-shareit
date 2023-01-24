package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    default ItemRequest extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующий для поиска нужной вещи запрос с id = " + id));
    }

    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(long requesterId);

    Page<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(long requesterId, Pageable pageable);

}

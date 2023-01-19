package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.*;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto findById(long userId, long id);

    Collection<ItemRequestDto> findAllByRequesterId(long requesterId);

    Page<ItemRequestDto> findAllByExpectRequesterId(long requesterId, Pageable pageable);

    ItemRequestShortDto create(ItemRequestIncomingDto itemRequestDto, long userId);


}

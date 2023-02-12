package ru.practicum.shareit.server.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.server.request.dto.*;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto findById(long userId, long id);

    List<ItemRequestDto> findAllByRequesterId(long requesterId);

    Page<ItemRequestDto> findAllByExpectRequesterId(long requesterId, Pageable pageable);

    ItemRequestShortDto create(ItemRequestIncomingDto itemRequestDto, long userId);
}

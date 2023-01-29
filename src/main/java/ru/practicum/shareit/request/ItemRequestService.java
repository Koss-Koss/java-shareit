package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.*;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto findById(long userId, long id);

    List<ItemRequestDto> findAllByRequesterId(long requesterId);

    Page<ItemRequestDto> findAllByExpectRequesterId(long requesterId, Pageable pageable);

    ItemRequestShortDto create(ItemRequestIncomingDto itemRequestDto, long userId);


}

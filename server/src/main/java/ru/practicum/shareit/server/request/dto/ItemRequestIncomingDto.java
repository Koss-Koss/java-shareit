package ru.practicum.shareit.server.request.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class ItemRequestIncomingDto {
    private String description;
}

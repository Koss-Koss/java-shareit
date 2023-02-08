package ru.practicum.shareit.server.request.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class ItemRequestIncomingDto {
    //@NotBlank(message = "Отсутствует описание запрошенной вещи")
    private String description;
}

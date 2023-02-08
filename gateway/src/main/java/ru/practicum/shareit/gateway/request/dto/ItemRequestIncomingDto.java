package ru.practicum.shareit.gateway.request.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
public class ItemRequestIncomingDto {
    @NotBlank(message = "Отсутствует описание запрошенной вещи")
    private String description;
}

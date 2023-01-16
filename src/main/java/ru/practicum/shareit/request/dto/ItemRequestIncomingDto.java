package ru.practicum.shareit.request.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class ItemRequestIncomingDto {
    @NotBlank(message = "Отсутствует описание запрошенной вещи")
    private String description;
}

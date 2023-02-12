package ru.practicum.shareit.gateway.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
public class CommentIncomingDto {
    @NotBlank(message = "Отсутствует текст комментария")
    private String text;
}

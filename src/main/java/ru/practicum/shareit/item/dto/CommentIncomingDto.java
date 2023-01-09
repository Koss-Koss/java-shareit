package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CommentIncomingDto {
    @NotBlank(message = "Отсутствует текст комментария")
    String text;
}

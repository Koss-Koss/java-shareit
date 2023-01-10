package ru.practicum.shareit.item.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class CommentIncomingDto {
    @NotBlank(message = "Отсутствует текст комментария")
    private String text;
}

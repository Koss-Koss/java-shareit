package ru.practicum.shareit.server.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class CommentIncomingDto {
    //@NotBlank(message = "Отсутствует текст комментария")
    private String text;
}

package ru.practicum.shareit.server.item.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class CommentIncomingDto {
    private String text;
}

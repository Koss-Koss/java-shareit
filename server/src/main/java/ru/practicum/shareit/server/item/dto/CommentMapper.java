package ru.practicum.shareit.server.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;

@Component
public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName((comment.getAuthor() != null) ? comment.getAuthor().getName() : "")
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentIncomingDto commentDto, User author, Item item) {
        return Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }
}
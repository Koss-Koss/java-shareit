package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.*;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void findAllByItem_IdOrderByCreatedDesc() {
        User saveAuthor = userRepository.save(User.builder().name("AuthorName").email("author@test.com").build());
        User saveOwner = userRepository.save(User.builder().name("OwnerName").email("owner@test.com").build());

        Item saveItem = itemRepository.save(Item.builder()
                .name("TestItemName")
                .description("ItemDescription")
                .available(true)
                .owner(saveOwner)
                .build());
        Item saveItem2 = itemRepository.save(Item.builder()
                .name("TestItem2Name")
                .description("Item2Description")
                .available(true)
                .owner(saveOwner)
                .build());
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItem3Name")
                .description("Item3Description")
                .available(true)
                .owner(saveOwner)
                .build());

        LocalDateTime created = LocalDateTime.now().plusMinutes(5);
        Comment saveComment = commentRepository.save(Comment.builder()
                .text("CommentText")
                .item(saveItem)
                .author(saveAuthor)
                .created(created)
                .build());
        commentRepository.save(Comment.builder()
                .text("Comment2Text")
                .item(saveItem2)
                .author(saveAuthor)
                .created(created.plusMinutes(20))
                .build());
        Comment saveComment2 = commentRepository.save(Comment.builder()
                .text("Comment3Text")
                .item(saveItem)
                .author(saveAuthor)
                .created(created.plusMinutes(10))
                .build());

        List<Comment> result = commentRepository.findAllByItem_IdOrderByCreatedDesc(saveItem.getId());
        assertEquals(2, result.size());
        assertEquals(saveComment2, result.get(0));
        assertEquals(saveComment, result.get(1));
        assertEquals(0, commentRepository.findAllByItem_IdOrderByCreatedDesc(saveItem3.getId()).size());
    }
}
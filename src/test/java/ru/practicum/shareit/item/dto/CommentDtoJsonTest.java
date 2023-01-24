package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void serialization() throws IOException {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("CommentText")
                .authorName("AuthorName")
                .created(LocalDateTime.of(2022, 3, 8, 11, 22, 33))
                .build();

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("CommentText");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("AuthorName");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2022-03-08T11:22:33");
    }
}
package ru.practicum.shareit.server.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoForNotOwnerJsonTest {

    @Autowired
    private JacksonTester<ItemDtoForNotOwner> json;

    @Test
    void serialization() throws IOException {
        ItemDtoForNotOwner itemDto = ItemDtoForNotOwner.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .requestId(7L)
                .comments(List.of())
                .build();

        JsonContent<ItemDtoForNotOwner> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(7);
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEqualTo(List.of());
    }
}

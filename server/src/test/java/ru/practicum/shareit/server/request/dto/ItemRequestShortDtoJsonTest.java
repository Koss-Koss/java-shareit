package ru.practicum.shareit.server.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemRequestShortDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestShortDto> json;

    @Test
    void serialization() throws IOException {
        ItemRequestShortDto itemRequestDto = ItemRequestShortDto.builder()
                .id(1L)
                .description("Description")
                .created(LocalDateTime.of(2022, 3, 8, 11, 22, 33))
                .build();

        JsonContent<ItemRequestShortDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2022-03-08T11:22:33");
    }
}

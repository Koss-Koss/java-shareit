package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void serialization() throws IOException {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .lastBooking(BookingShortDto.builder().id(3L).bookerId(5L).build())
                .nextBooking(BookingShortDto.builder().id(11L).bookerId(2L).build())
                .requestId(7L)
                .comments(List.of())
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathValue("$.lastBooking.id", BookingShortDto.class).isEqualTo(3);
        assertThat(result).extractingJsonPathValue("$.lastBooking.bookerId", BookingShortDto.class).isEqualTo(5);
        assertThat(result).extractingJsonPathValue("$.nextBooking.id", BookingShortDto.class).isEqualTo(11);
        assertThat(result).extractingJsonPathValue("$.nextBooking.bookerId", BookingShortDto.class).isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(7);
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEqualTo(List.of());
    }
}
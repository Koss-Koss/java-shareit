package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serialization() throws IOException {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2022, 3, 8, 11, 22, 33))
                .end(LocalDateTime.of(2023, 4, 9, 12, 33, 44))
                .item(ItemDto.builder()
                        .id(3L)
                        .name("ItemName")
                        .description("ItemDescription")
                        .available(true)
                        .lastBooking(BookingShortDto.builder().id(5L).bookerId(2L).build())
                        .nextBooking(BookingShortDto.builder().id(6L).bookerId(4L).build())
                        .comments(List.of())
                        .requestId(8L)
                        .build())
                .booker(UserDto.builder().id(12L).name("Booker").email("booker@test.com").build())
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2022-03-08T11:22:33");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-04-09T12:33:44");
        assertThat(result).extractingJsonPathValue("$.item.id", ItemDto.class).isEqualTo(3);
        assertThat(result).extractingJsonPathValue("$.item.name", ItemDto.class).isEqualTo("ItemName");
        assertThat(result).extractingJsonPathValue("$.item.description", ItemDto.class).isEqualTo("ItemDescription");
        assertThat(result).extractingJsonPathValue("$.item.available", ItemDto.class).isEqualTo(true);
        assertThat(result).extractingJsonPathValue("$.item.lastBooking.id", ItemDto.class).isEqualTo(5);
        assertThat(result).extractingJsonPathValue("$.item.lastBooking.bookerId", ItemDto.class).isEqualTo(2);
        assertThat(result).extractingJsonPathValue("$.item.nextBooking.id", ItemDto.class).isEqualTo(6);
        assertThat(result).extractingJsonPathValue("$.item.nextBooking.bookerId", ItemDto.class).isEqualTo(4);
        assertThat(result).extractingJsonPathValue("$.item.comments", ItemDto.class).isEqualTo(List.of());
        assertThat(result).extractingJsonPathValue("$.item.requestId", ItemDto.class).isEqualTo(8);
        assertThat(result).extractingJsonPathValue("$.booker.id", UserDto.class).isEqualTo(12);
        assertThat(result).extractingJsonPathValue("$.booker.name", UserDto.class).isEqualTo("Booker");
        assertThat(result).extractingJsonPathValue("$.booker.email", UserDto.class).isEqualTo("booker@test.com");
        assertThat(result).extractingJsonPathValue("$.status", BookingStatus.class).isEqualTo("WAITING");
    }
}
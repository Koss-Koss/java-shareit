package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingIncomingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingIncomingDto> json;

    @Test
    void deserialization() throws IOException {
        String inputJson = "{\"itemId\":1,\"start\":\"2022-03-08T11:22:33\",\"end\":\"2023-04-09T12:33:44\"}";
        BookingIncomingDto expectedBookingIncomingDto = BookingIncomingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2022, 3, 8, 11, 22, 33))
                .end(LocalDateTime.of(2023, 4, 9, 12, 33, 44))
                .build();

        BookingIncomingDto bookingIncomingDto = json.parseObject(inputJson);

        assertThat(bookingIncomingDto).isEqualTo(expectedBookingIncomingDto);
    }
}
package ru.practicum.shareit.server.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.server.exception.InvalidConditionException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.server.ShareItServerConstants.COMMON_USER_PATH;
import static ru.practicum.shareit.server.user.UserController.USER_PREFIX;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    long id = 1;
    UserDto userDto = UserDto.builder().name("TestName").email("test@test.com").build();
    UserDto expectedUserDto = userDto.toBuilder().id(id).build();

    @Test
    @DisplayName("Метод getUserById - Успех")
    void getUserById_whenValidUserId_thenResponseStatusOkWithUserDtoInBody() throws Exception {
        when(userService.findById(anyLong()))
                .thenReturn(expectedUserDto);

        mvc.perform(get(COMMON_USER_PATH + USER_PREFIX, id)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedUserDto)));
        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Метод getUserById - Плохой userId")
    void getUserById_whenInvalidUserId_thenResponseStatusNotFound() throws Exception {
        when(userService.findById(anyLong()))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_USER_PATH + USER_PREFIX, id)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Метод getAllUsers - Успех")
    void getAllUsers_whenInvoked_thenResponseStatusOkWithUsersDtoCollectionInBody() throws Exception {
        when(userService.findAll())
                .thenReturn(Collections.singletonList(expectedUserDto));

        mvc.perform(get(COMMON_USER_PATH)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Collections.singletonList(expectedUserDto))));
        verify(userService, times(1)).findAll();
    }


    @Test
    @DisplayName("Метод create - Успех")
    void create_whenValidUser_thenResponseStatusOkWithUserDtoInBody() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenReturn(expectedUserDto);

        mvc.perform(post(COMMON_USER_PATH)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedUserDto)));
        verify(userService, times(1)).create(any(UserDto.class));
    }

    @Test
    @DisplayName("Метод create - Плохие входные данные")
    void create_whenInvalidUser_thenResponseStatusBadRequest() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(post(COMMON_USER_PATH)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(userService, times(1)).create(any(UserDto.class));
    }

    @Test
    @DisplayName("Метод update - Успех")
    void update_whenValidUser_thenResponseStatusOkWithUserDtoInBody() throws Exception {
        when(userService.update(anyLong(), any(UserDto.class)))
                .thenReturn(expectedUserDto);

        mvc.perform(patch(COMMON_USER_PATH + USER_PREFIX, id)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedUserDto)));
        verify(userService, times(1)).update(anyLong(), any(UserDto.class));
    }

    @Test
    @DisplayName("Метод update - Плохие входные данные")
    void update_whenInvalidUser_thenResponseStatusInternalServerError() throws Exception {
        when(userService.update(anyLong(), any(UserDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(patch(COMMON_USER_PATH + USER_PREFIX, id)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(userService, times(1)).update(anyLong(), any(UserDto.class));
    }

    @Test
    @DisplayName("Метод update - Плохой userId")
    void update_whenInvalidUserId_thenResponseStatusNotFound() throws Exception {
        when(userService.update(anyLong(), any(UserDto.class)))
                .thenThrow(NotFoundException.class);

        mvc.perform(patch(COMMON_USER_PATH + USER_PREFIX, id)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).update(anyLong(), any(UserDto.class));
    }

    @Test
    @DisplayName("Метод delete - Успех")
    void delete_whenValidUserId_thenResponseStatusOk() throws Exception {

        mvc.perform(delete(COMMON_USER_PATH + USER_PREFIX, id)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
        verify(userService, times(1)).delete(anyLong());
    }

    @Test
    @DisplayName("Метод delete - Плохой userId")
    void delete_whenInvalidUserId_thenResponseStatusNotFound() throws Exception {
        doThrow(NotFoundException.class).when(userService).delete(anyLong());

        mvc.perform(delete(COMMON_USER_PATH + USER_PREFIX, id)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).delete(anyLong());
    }
}

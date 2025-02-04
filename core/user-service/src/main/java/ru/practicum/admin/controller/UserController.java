package ru.practicum.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.DataTransferConvention;
import ru.practicum.admin.service.UserService;
import ru.practicum.interaction.client.UserClient;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;

import java.util.List;


@RestController
@RequestMapping("/admin/users")
@Validated
@RequiredArgsConstructor
public class UserController implements UserClient {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(@RequestParam(required = false) List<Long> ids,
                                                  @RequestParam(required = false,
                                                      defaultValue = DataTransferConvention.FROM)
                                                  Integer from,
                                                  @RequestParam(required = false,
                                                      defaultValue = DataTransferConvention.SIZE)
                                                  Integer size) {
        return new ResponseEntity<>(userService.getUsers(ids, from, size), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserDto> addUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        return new ResponseEntity<>(userService.addUser(newUserRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

package ru.practicum.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.DataTransferConvention;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {

    @GetMapping
    ResponseEntity<List<UserDto>> getUsers(@RequestParam(required = false) List<Long> ids,
                                           @RequestParam(required = false,
                                               defaultValue = DataTransferConvention.FROM)
                                           Integer from,
                                           @RequestParam(required = false,
                                               defaultValue = DataTransferConvention.SIZE)
                                           Integer size);

    @PostMapping(consumes = "application/json")
    ResponseEntity<UserDto> addUser(@RequestBody @Valid NewUserRequest newUserRequest);

    @DeleteMapping("/{userId}")
    ResponseEntity<Object> deleteUser(@PathVariable Long userId);
}

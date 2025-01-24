package ru.practicum.interaction.dto.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.interaction.model.User;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface NewUserRequestMapper {
    @Mapping(target = "id", ignore = true)
    User toUser(NewUserRequest newUserRequest);
}

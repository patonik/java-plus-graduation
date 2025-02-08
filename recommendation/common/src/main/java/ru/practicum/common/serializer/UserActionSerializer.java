package ru.practicum.common.serializer;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionSerializer extends BaseAvroSerializer<UserActionAvro> {


    public UserActionSerializer() {
        super(UserActionAvro.getClassSchema());
    }
}


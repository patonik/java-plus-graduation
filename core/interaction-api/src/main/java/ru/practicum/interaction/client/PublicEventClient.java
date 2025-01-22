package ru.practicum.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.interaction.dto.event.EventFullDto;

@FeignClient(name = "event-service", path = "/events", contextId = "publicEventClient")
public interface PublicEventClient {

    @GetMapping("/internal/{id}")
    ResponseEntity<EventFullDto> getEvent(@PathVariable Long id);
}

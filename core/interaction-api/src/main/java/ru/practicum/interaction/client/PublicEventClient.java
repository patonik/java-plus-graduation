package ru.practicum.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.DataTransferConvention;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.SortCriterium;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "event-service", path = "/events", configuration = FeignConfig.class)
public interface PublicEventClient {


    @GetMapping
    ResponseEntity<List<EventShortDto>> getEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) Long[] categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN)
                                                  LocalDateTime rangeStart,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN)
                                                  LocalDateTime rangeEnd,
                                                  @RequestParam(required = false, defaultValue = "false")
                                                  Boolean onlyAvailable,
                                                  @RequestParam(required = false) SortCriterium sort,
                                                  @RequestParam(required = false,
                                                      defaultValue = DataTransferConvention.FROM)
                                                  Integer from,
                                                  @RequestParam(required = false,
                                                      defaultValue = DataTransferConvention.SIZE)
                                                  Integer size);

    @GetMapping("/{id}")
    ResponseEntity<EventFullDto> getEvent(@PathVariable Long id);
}

package ru.practicum.repository;

import ru.practicum.stats.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface HitListElementRepository {
    List<StatResponseDto> getHitListElementDtos(LocalDateTime start,
                                                LocalDateTime end,
                                                String[] uris,
                                                boolean unique);
}

package ru.practicum.pub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.client.RequestClient;
import ru.practicum.stats.HttpStatsClient;
import ru.practicum.interaction.dto.compilation.CompilationDto;
import ru.practicum.interaction.dto.compilation.CompilationDtoMapper;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.model.Compilation;
import ru.practicum.pub.repository.PublicCompilationRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationService {
    private final PublicCompilationRepository publicCompilationRepository;
    private final HttpStatsClient httpStatsClient;
    private final CompilationDtoMapper compilationDtoMapper;
    private final RequestClient requestClient;

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Compilation> compilations = publicCompilationRepository.findAllCompilations(pinned, pageable);
        if (compilations.isEmpty()) {
            return List.of();
        }
        List<CompilationDto> compilationDtos = compilationDtoMapper.toCompilationDtoList(compilations);
        Set<EventShortDto> eventShortDtoSet = compilationDtos
                .stream()
                .flatMap(x -> x == null ? Stream.empty() : x.getEvents().stream())
                .collect(Collectors.toSet());
        if (!eventShortDtoSet.isEmpty()) {
            publicCompilationRepository.populateEventShortDtos(
                    eventShortDtoSet, httpStatsClient, requestClient);
        }
        return compilationDtos;
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation =
                publicCompilationRepository.findById(compId)
                        .orElseThrow(() -> new NotFoundException("Compilation with id " + compId + " not found"));
        return compilationDtoMapper.toCompilationDto(compilation);
    }
}

package ru.practicum.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.admin.repository.AdminCompilationRepository;
import ru.practicum.admin.repository.AdminEventRepository;
import ru.practicum.interaction.dto.compilation.CompilationDto;
import ru.practicum.interaction.dto.compilation.CompilationDtoMapper;
import ru.practicum.interaction.dto.compilation.NewCompilationDto;
import ru.practicum.interaction.dto.compilation.UpdateCompilationRequest;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.model.Compilation;
import ru.practicum.interaction.model.Event;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationService {
    private final AdminCompilationRepository adminCompilationRepository;
    private final AdminEventRepository adminEventRepository;
    private final CompilationDtoMapper compilationDtoMapper;

    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Set<Long> eventIds = newCompilationDto.getEventIds();
        Set<Event> events = adminEventRepository.findAllByIdIn(eventIds);
        if (events.size() != eventIds.size()) {
            throw new ConflictException(
                "Event/-s is/are missing with one or more of your ids " + eventIds);
        }
        Compilation compilation =
            new Compilation(null, newCompilationDto.getPinned(), newCompilationDto.getTitle(), events);
        adminCompilationRepository.save(compilation);
        return compilationDtoMapper.toCompilationDto(compilation);
    }

    public void deleteCompilation(Long compId) {
        boolean exists = adminCompilationRepository.existsById(compId);
        if (!exists) {
            throw new NotFoundException("No compilation with id " + compId);
        }
        adminCompilationRepository.deleteById(compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto) {
        Compilation compilation = adminCompilationRepository.findById(compId)
            .orElseThrow(() -> new NotFoundException("No compilation with id " + compId));
        Set<Event> events = adminEventRepository.findAllByIdIn(compilationDto.getEventIds());
        compilation =
            adminCompilationRepository.save(
                compilationDtoMapper.updateCompilation(compilationDto, compilation, events));
        return compilationDtoMapper.toCompilationDto(compilation);
    }
}

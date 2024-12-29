package ru.practicum.ewm.compilations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.entity.Compilation;
import ru.practicum.ewm.events.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    Compilation toEntity(NewCompilationDto newCompilationDto, List<Event> events);

    CompilationDto toDto(Compilation save);
}

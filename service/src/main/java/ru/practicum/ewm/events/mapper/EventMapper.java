package ru.practicum.ewm.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.categories.entity.Category;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.locations.Location;
import ru.practicum.ewm.user.entity.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    EventFullDto toDto(Event event);

    @Mapping(target = "views", source = "views")
    EventFullDtoWithViews toDtoWithView(Event event, long views);

    @Mappings({
            @Mapping(target = "category", source = "category"),
            @Mapping(target = "initiator", source = "initiator"),
            @Mapping(target = "location", source = "location"),
            @Mapping(target = "id", ignore = true)
    })
    Event toEntity(NewEventDto newEventDto, User initiator, Category category, Location location);

    EventShortDto toShortDto(Event event);

    EventShortDtoWithViews toShortViewDto(Event event);

    UpdateEventAdminRequest toAdminOrUser(UpdateEventUserRequest updateEvent);

    EventShortDtoWithViews toEventShortDtoWithViews(Event event, Long hits, Long orDefault);
}

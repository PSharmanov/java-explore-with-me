package ru.practicum.ewm.comments.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDto toDto(Comment comment);
}

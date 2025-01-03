package ru.practicum.ewm.comments.service;

import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    void deleteCommentByAuthor(Long userId, Long commentId);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto);

    List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size);

    CommentDto getCommentById(Long commentId);

    void deleteCommentByAdmin(Long commentId);
}

package ru.practicum.ewm.comments;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentControllerPublic {
    private final CommentService commentService;

    @GetMapping("/event/{eventId}")
    public List<CommentDto> getComments(@PathVariable Long eventId,
                                        @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero
                                        Integer from,
                                        @RequestParam(value = "size", defaultValue = "10") @Positive
                                        Integer size) {
        return commentService.getCommentsByEventId(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }
}

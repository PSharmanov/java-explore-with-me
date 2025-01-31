package ru.practicum.ewm.comments;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.service.CommentService;


@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentControllerAdmin {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}

package ru.practicum.ewm.comments.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @Column(nullable = false)
    String text;

    @CreationTimestamp
    @Column(name = "created", nullable = false, updatable = false)
    LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    LocalDateTime updated;

}

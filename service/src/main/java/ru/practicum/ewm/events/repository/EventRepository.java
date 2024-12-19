package ru.practicum.ewm.events.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.categories.entity.Category;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;
import ru.practicum.ewm.requests.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByInitiatorId(Long userId, Pageable page);

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:rangeStart as timestamp ) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd as timestamp ) IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findEventsByAdmin(List<Long> users,
                                  List<String> states,
                                  List<Long> categories,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Pageable page);

    boolean existsByCategory(Category category);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT new ru.practicum.ewm.requests.dto.ConfirmedRequests(COUNT(DISTINCT r.id), r.event.id) " +
            "FROM ParticipationRequest AS r " +
            "WHERE r.event.id IN (:ids) AND r.status = :status " +
            "GROUP BY (r.event)")
    List<ConfirmedRequests> findAllByEventIdInAndStatus(@Param("ids") List<Long> ids, @Param("status") RequestStatus status);

    Page<Event> findAll(Specification<Event> specification, Pageable pageable);
}



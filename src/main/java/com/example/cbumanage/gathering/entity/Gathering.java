package com.example.cbumanage.gathering.entity;

import com.example.cbumanage.gathering.entity.enums.GatheringType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "gathering")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gathering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatheringType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime gatheringDate;

    private String location;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    private LocalDateTime voteDeadline;

    @Column(nullable = false)
    private Boolean allMembersTarget;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public boolean isVoteClosed() {
        return voteDeadline != null && LocalDateTime.now().isAfter(voteDeadline);
    }

    public static Gathering create(String title, GatheringType type, String description,
                                   LocalDateTime gatheringDate, String location, LocalDateTime voteDeadline,
                                   Boolean allMembersTarget, Long authorId) {
        Gathering gathering = new Gathering();
        gathering.title = title;
        gathering.type = type;
        gathering.description = description;
        gathering.gatheringDate = gatheringDate;
        gathering.location = location;
        gathering.voteDeadline = voteDeadline;
        gathering.allMembersTarget = allMembersTarget;
        gathering.authorId = authorId;
        gathering.isDeleted = false;
        return gathering;
    }

    public void update(String title, String description,
                       LocalDateTime gatheringDate, String location, LocalDateTime voteDeadline) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (gatheringDate != null) this.gatheringDate = gatheringDate;
        if (location != null) this.location = location;
        this.voteDeadline = voteDeadline;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void close() {
        this.voteDeadline = LocalDateTime.now();
    }
}

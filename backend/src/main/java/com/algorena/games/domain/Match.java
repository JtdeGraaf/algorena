package com.algorena.games.domain;

import com.algorena.bots.domain.Game;
import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "game", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchParticipant> participants = new ArrayList<>();

    @Nullable
    @Column(name = "forfeit_reason", length = 50)
    private String forfeitReason;

    public void start() {
        this.status = MatchStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void finish() {
        this.status = MatchStatus.FINISHED;
        this.finishedAt = LocalDateTime.now();
    }

    public void abort() {
        this.status = MatchStatus.ABORTED;
        this.finishedAt = LocalDateTime.now();
    }

    public void addParticipant(MatchParticipant participant) {
        this.participants.add(participant);
    }

    public void forfeit(String reason) {
        this.forfeitReason = reason;
        this.status = MatchStatus.FINISHED;
        this.finishedAt = LocalDateTime.now();
    }
}

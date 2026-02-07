package com.algorena.bots.data;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
    List<Bot> findByUserIdAndDeletedFalse(Long userId);

    Optional<Bot> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    @Query("SELECT b FROM Bot b WHERE " +
            "b.deleted = false AND " +
            "(:userId IS NULL OR b.userId = :userId) AND " +
            "(COALESCE(:name, '') = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:game IS NULL OR b.game = :game) AND " +
            "(:active IS NULL OR b.active = :active)")
    Page<Bot> findByFilters(@Nullable Long userId,
                            @Nullable String name,
                            @Nullable Game game,
                            @Nullable Boolean active,
                            Pageable pageable);
}


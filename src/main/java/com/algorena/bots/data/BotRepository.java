package com.algorena.bots.data;

import com.algorena.bots.domain.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
    List<Bot> findByUserId(Long userId);

    Optional<Bot> findByIdAndUserId(Long id, Long userId);
}


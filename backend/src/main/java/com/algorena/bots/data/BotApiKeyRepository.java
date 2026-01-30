package com.algorena.bots.data;

import com.algorena.bots.domain.BotApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BotApiKeyRepository extends JpaRepository<BotApiKey, Long> {
    List<BotApiKey> findByBotId(Long botId);

    Optional<BotApiKey> findByKeyHash(String keyHash);
}


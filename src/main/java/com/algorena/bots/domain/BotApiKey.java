package com.algorena.bots.domain;

import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Entity
@Table(name = "bot_api_keys")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bot_id", nullable = false)
    private Long botId;

    @Column(name = "key_hash", nullable = false, unique = true)
    private String keyHash;

    @Column(name = "key_prefix", nullable = false, length = 10)
    private String keyPrefix;

    @Nullable
    @Column(name = "name", length = 50)
    private String name;

    @Nullable
    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Nullable
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    public void revoke() {
        this.revoked = true;
    }

    public void markAsUsed() {
        this.lastUsed = LocalDateTime.now();
    }

    public boolean isValid() {
        if (revoked) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }
}


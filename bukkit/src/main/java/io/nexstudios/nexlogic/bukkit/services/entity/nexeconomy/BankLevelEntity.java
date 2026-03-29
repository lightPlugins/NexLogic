package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents the current level progression of a bank account.
 * Stores the level and related metadata for a specific bank account.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nex_economy_bank_level",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_bank_level_account",
            columnNames = {"bank_account_id"}
        )
    },
    indexes = {
        @Index(name = "idx_bank_level_account", columnList = "bank_account_id")
    }
)
public class BankLevelEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "bank_account_id", nullable = false, updatable = false)
  private UUID bankAccountId;

  @Column(name = "current_level", nullable = false)
  private int currentLevel;

  @Version
  @Column(name = "row_version", nullable = false)
  private long rowVersion;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
    if (currentLevel <= 0) currentLevel = 1;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
    if (currentLevel <= 0) currentLevel = 1;
  }
}


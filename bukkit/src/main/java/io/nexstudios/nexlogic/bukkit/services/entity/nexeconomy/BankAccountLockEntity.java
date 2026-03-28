package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "bank_account_lock",
    indexes = {
        @Index(name = "idx_bank_account_lock_player", columnList = "player_uuid", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountLockEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "player_uuid", nullable = false, unique = true)
  private UUID playerUuid;

  @Column(name = "locked_by_uuid", nullable = false)
  private UUID lockedByUuid;

  @Column(name = "locked_at", nullable = false)
  private Instant lockedAt;

  @Column(name = "reason", nullable = true, length = 256)
  private String reason;
}
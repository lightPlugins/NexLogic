package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nex_bank_unlocks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bankIdLower", "ownerUuid"})
)
public class BankUnlockEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, length = 64)
  private String bankIdLower;

  @Column(nullable = false)
  private UUID ownerUuid;

  @Column(nullable = false)
  private UUID unlockedByUuid;

  @Column(nullable = false)
  private Instant unlockedAt;
}
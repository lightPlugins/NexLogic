package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit/transaction log for bank accounts.
 * Useful for /bank log and support/debugging.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nex_economy_bank_tx",
    indexes = {
        @Index(name = "idx_bank_tx_account_time", columnList = "bank_account_id,created_at"),
        @Index(name = "idx_bank_tx_actor", columnList = "actor_uuid")
    }
)
public class BankTransactionEntity {

  public enum Type {
    DEPOSIT,
    WITHDRAW,
    INVITE_SENT,
    INVITE_ACCEPTED,
    MEMBER_KICKED,
    ROLE_CHANGED,
    LEVEL_UPGRADED,
    INTEREST_APPLIED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "bank_account_id", nullable = false, updatable = false)
  private UUID bankAccountId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, updatable = false, length = 32)
  private Type type;

  @Column(name = "actor_uuid")
  private UUID actorUuid;

  @Column(name = "target_uuid")
  private UUID targetUuid;

  /**
   * Amount involved (if any) in MantissaAmount storage format.
   */
  @Column(name = "amount_mantissa", nullable = false, length = 128)
  private String amountMantissa;

  @Column(name = "amount_exp3", nullable = false)
  private int amountExp3;

  /**
   * Optional: store a small message context (e.g. oldRole->newRole).
   */
  @Column(name = "meta", length = 512)
  private String meta;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
    if (amountMantissa == null || amountMantissa.isBlank()) amountMantissa = "0";
  }
}
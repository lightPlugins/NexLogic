package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents one bank account owned by exactly one player for exactly one bank definition (bankId).
 *
 * Storage:
 * - bankIdLower: derived from the bank file name (without .yml), normalized to lower-case.
 * - ownerUuid: the immutable "real owner" of this bank account (cannot be removed).
 * - balance: stored using the MantissaAmount storage format (mantissaText + exp3).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nex_economy_bank_account",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_bank_account_bank_owner", columnNames = {"bank_id_lower", "owner_uuid"})
    },
    indexes = {
        @Index(name = "idx_bank_account_owner", columnList = "owner_uuid"),
        @Index(name = "idx_bank_account_bank", columnList = "bank_id_lower")
    }
)
public class BankAccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "bank_id_lower", nullable = false, updatable = false, length = 64)
  private String bankIdLower;

  @Column(name = "owner_uuid", nullable = false, updatable = false)
  private UUID ownerUuid;

  @Column(name = "level", nullable = false)
  private int level;

  @Column(name = "balance_mantissa", nullable = false, length = 128)
  private String balanceMantissa;

  @Column(name = "balance_exp3", nullable = false)
  private int balanceExp3;

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

    if (level <= 0) level = 1;
    if (balanceMantissa == null || balanceMantissa.isBlank()) balanceMantissa = "0";
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
    if (balanceMantissa == null || balanceMantissa.isBlank()) balanceMantissa = "0";
    if (level <= 0) level = 1;
  }
}
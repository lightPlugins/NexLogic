package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks per-member withdraw usage for a specific bank account.
 *
 * This is required to enforce hourly/daily limits in a multi-server setup reliably.
 * Amount is stored with MantissaAmount storage fields.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nex_economy_bank_withdraw_usage",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_bank_withdraw_usage_account_member_window",
            columnNames = {"bank_account_id", "member_uuid", "window_type", "window_start_epoch"}
        )
    },
    indexes = {
        @Index(name = "idx_bank_withdraw_usage_account_member", columnList = "bank_account_id,member_uuid")
    }
)
public class BankWithdrawUsageEntity {

  public enum WindowType {
    HOURLY,
    DAILY
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "bank_account_id", nullable = false, updatable = false)
  private UUID bankAccountId;

  @Column(name = "member_uuid", nullable = false, updatable = false)
  private UUID memberUuid;

  @Enumerated(EnumType.STRING)
  @Column(name = "window_type", nullable = false, updatable = false, length = 16)
  private WindowType windowType;

  /**
   * Start of the window in epoch seconds (e.g. hour start or day start in the bank timezone).
   */
  @Column(name = "window_start_epoch", nullable = false, updatable = false)
  private long windowStartEpoch;

  @Column(name = "used_mantissa", nullable = false, length = 128)
  private String usedMantissa;

  @Column(name = "used_exp3", nullable = false)
  private int usedExp3;

  @Version
  @Column(name = "row_version", nullable = false)
  private long rowVersion;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    if (usedMantissa == null || usedMantissa.isBlank()) usedMantissa = "0";
    if (updatedAt == null) updatedAt = Instant.now();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
    if (usedMantissa == null || usedMantissa.isBlank()) usedMantissa = "0";
  }
}
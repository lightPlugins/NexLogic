package io.nexstudios.nexlogic.bukkit.services.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "nexlogic_economy_balance",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_economy_uuid_currency_type",
            columnNames = {"player_uuid", "currency", "account_type"}
        )
    },
    indexes = {
        @Index(name = "idx_economy_player_uuid", columnList = "player_uuid"),
        @Index(name = "idx_economy_currency", columnList = "currency"),
        @Index(name = "idx_economy_account_type", columnList = "account_type")
    }
)
public class EconomyBalanceEntity {

  public enum EconomyAccountType {
    PLAYER,
    TOWNY
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "player_uuid", nullable = false, updatable = false)
  private UUID playerUuid;

  @Column(name = "currency", nullable = false, length = 64)
  private String currency;

  @Column(name = "amount", nullable = false, columnDefinition = "TEXT")
  private String amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false, length = 16)
  private EconomyAccountType accountType;

  // Base-1000 Exponent
  @Column(name = "amount_exp3", nullable = false)
  private int amountExp3;

  @PrePersist
  @PreUpdate
  private void normalize() {
    if (currency != null) {
      currency = currency.trim().toLowerCase();
    }

    if (accountType == null) {
      accountType = EconomyAccountType.PLAYER;
    }

    if (amount == null || amount.isBlank()) {
      amount = "0";
      amountExp3 = 0;
      return;
    }

    amount = amount.trim();
  }
}
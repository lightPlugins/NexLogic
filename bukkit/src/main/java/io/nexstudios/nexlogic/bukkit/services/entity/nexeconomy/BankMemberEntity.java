package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a membership entry for a specific bank account.
 *
 * Rules (enforced in service layer, not DB):
 * - The real owner (BankAccountEntity.ownerUuid) must always exist as a member and cannot be removed.
 * - Role priority rules apply when kicking/changing roles.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nex_economy_bank_member",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_bank_member_account_member",
            columnNames = {"bank_account_id", "member_uuid"}
        )
    },
    indexes = {
        @Index(name = "idx_bank_member_account", columnList = "bank_account_id"),
        @Index(name = "idx_bank_member_member", columnList = "member_uuid")
    }
)
public class BankMemberEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "bank_account_id", nullable = false, updatable = false)
  private UUID bankAccountId;

  @Column(name = "member_uuid", nullable = false, updatable = false)
  private UUID memberUuid;

  /**
   * Role id as defined in the bank yml file (e.g. "owner", "member", "noob").
   */
  @Column(name = "role_id_lower", nullable = false, length = 64)
  private String roleIdLower;

  @Column(name = "added_by_uuid")
  private UUID addedByUuid;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @Version
  @Column(name = "row_version", nullable = false)
  private long rowVersion;

  @PrePersist
  void prePersist() {
    if (joinedAt == null) joinedAt = Instant.now();
    if (roleIdLower == null || roleIdLower.isBlank()) roleIdLower = "member";
  }
}
package io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent bank invites.
 * Allows /bank invite + /bank accept to survive restarts and work across servers.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nex_economy_bank_invite",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_bank_invite_account_invitee",
            columnNames = {"bank_account_id", "invitee_uuid"}
        )
    },
    indexes = {
        @Index(name = "idx_bank_invite_invitee", columnList = "invitee_uuid"),
        @Index(name = "idx_bank_invite_account", columnList = "bank_account_id")
    }
)
public class BankInviteEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "bank_account_id", nullable = false, updatable = false)
  private UUID bankAccountId;

  @Column(name = "invitee_uuid", nullable = false, updatable = false)
  private UUID inviteeUuid;

  @Column(name = "invited_by_uuid", nullable = false, updatable = false)
  private UUID invitedByUuid;

  @Column(name = "role_id_lower", nullable = false, length = 64)
  private String roleIdLower;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Version
  @Column(name = "row_version", nullable = false)
  private long rowVersion;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
    if (roleIdLower == null || roleIdLower.isBlank()) roleIdLower = "member";
  }
}
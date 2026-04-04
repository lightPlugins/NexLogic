package io.nexstudios.nexlogic.bukkit.services.entity.heads;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "nexlogic_player_head",
    indexes = {
        @Index(name = "idx_player_head_name", columnList = "player_name"),
        @Index(name = "idx_player_head_updated_at", columnList = "updated_at")
    }
)
public class HeadEntity {

  @Id
  @Column(name = "player_uuid", nullable = false, updatable = false)
  private UUID playerUuid;

  @Column(name = "player_name", length = 64)
  private String playerName;

  @Column(name = "texture_base64", nullable = false, columnDefinition = "TEXT")
  private String textureBase64;

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
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}


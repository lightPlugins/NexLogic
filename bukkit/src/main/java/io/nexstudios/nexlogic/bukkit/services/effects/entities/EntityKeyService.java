package io.nexstudios.nexlogic.bukkit.services.effects.entities;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.entity.Entity;

import java.util.Set;

/**
 * The {@code EntityKeyService} interface defines operations for managing and working with
 * entity keys. Entity keys provide a unique, namespace-prefixed identifier (e.g., "minecraft:entity_name")
 * for entities in a platform-agnostic format. Implementations of this interface can enable
 * functionality like resolving keys, normalizing identifiers, and checking entity key validation
 * against a whitelist of allowed keys.
 */
public interface EntityKeyService extends Service {

  /**
   * Retrieves the key representation of the specified entity. The key serves as a unique identifier
   * for the entity, typically using a namespace-prefixed format (e.g., "minecraft:entity_name").
   *
   * @param entity the entity for which the key should be retrieved; must not be null
   * @return the key of the entity as a string, or a default value (e.g., "minecraft:unknown") if no key is available
   */
  String keyOf(Entity entity);

  /**
   * Normalizes a given identifier string by ensuring it is in a consistent format.
   * If the identifier does not contain a namespace prefix (e.g., "minecraft:"),
   * it automatically adds the "minecraft:" prefix. Additionally, the method trims
   * and converts the string to lowercase.
   *
   * @param id the identifier string to normalize; may include a namespace prefix or be a shorthand value
   * @return the normalized identifier string in lowercase with a namespace prefix if absent, or an empty string if the input is null or blank
   */
  String normalize(String id);

  /**
   * Evaluates whether the specified entity matches any of the provided allowed keys.
   * If either the entity is null or the set of allowed keys is null or empty, the method
   * returns a predefined outcome. Otherwise, it verifies if the key of the entity is
   * present in the allowed keys set.
   *
   * @param entity the entity to be checked; may be null
   * @param allowedKeysNormalized a set of normalized keys that are allowed;
   *                              if null or empty, the method will return true
   * @return true if the entity's key is in the allowed keys set, or if the set is null/empty;
   *         false if the entity is null or the key does not match
   */
  default boolean matches(Entity entity, Set<String> allowedKeysNormalized) {
    if (entity == null) return false;
    if (allowedKeysNormalized == null || allowedKeysNormalized.isEmpty()) return true;
    return allowedKeysNormalized.contains(keyOf(entity));
  }


}

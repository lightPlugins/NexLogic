package io.nexstudios.nexlogic.common.placeholder;

@FunctionalInterface
public interface PlaceholderProvider {
  String resolve(PlaceholderResolveContext ctx);
}
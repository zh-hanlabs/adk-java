/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.adk.models;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.adk.JsonBaseModel;
import com.google.auto.value.AutoValue;
import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Context cache state carried on an LLM response, and on its event, from one request to the next.
 *
 * <p>Metadata for an active cache has a {@link #cacheName()}, {@link #expireTime()} and {@link
 * #invocationsUsed()}. Fingerprint-only metadata has none of them: it records the cacheable prefix
 * so that a later request can tell whether the prefix is still the same.
 */
@AutoValue
@JsonDeserialize(builder = CacheMetadata.Builder.class)
public abstract class CacheMetadata extends JsonBaseModel {

  CacheMetadata() {}

  /** Hash of the cacheable state: system instruction, tools and the cached leading contents. */
  @JsonProperty("fingerprint")
  public abstract String fingerprint();

  /** Number of leading contents in the cache, or in the fingerprint when no cache is active. */
  @JsonProperty("contentsCount")
  public abstract int contentsCount();

  /** Full resource name of the cached content; empty when no cache is active. */
  @JsonProperty("cacheName")
  public abstract Optional<String> cacheName();

  /** When the cache expires; empty when no cache is active. */
  @JsonProperty("expireTime")
  public abstract Optional<Instant> expireTime();

  /** Number of invocations that have used the cache; empty when no cache is active. */
  @JsonProperty("invocationsUsed")
  public abstract Optional<Integer> invocationsUsed();

  /** When the cache was created; empty when no cache is active. */
  @JsonProperty("createdAt")
  public abstract Optional<Instant> createdAt();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_CacheMetadata.Builder();
  }

  /**
   * Builder for {@link CacheMetadata}. The snake_case aliases read metadata that ADK Python wrote.
   */
  @AutoValue.Builder
  @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
  public abstract static class Builder {

    @JsonCreator
    static Builder jacksonBuilder() {
      return CacheMetadata.builder();
    }

    @JsonProperty("fingerprint")
    public abstract Builder fingerprint(String fingerprint);

    @JsonProperty("contentsCount")
    @JsonAlias("contents_count")
    public abstract Builder contentsCount(int contentsCount);

    @JsonProperty("cacheName")
    @JsonAlias("cache_name")
    public abstract Builder cacheName(@Nullable String cacheName);

    @JsonProperty("expireTime")
    @JsonAlias("expire_time")
    public abstract Builder expireTime(@Nullable Instant expireTime);

    @JsonProperty("invocationsUsed")
    @JsonAlias("invocations_used")
    public abstract Builder invocationsUsed(@Nullable Integer invocationsUsed);

    @JsonProperty("createdAt")
    @JsonAlias("created_at")
    public abstract Builder createdAt(@Nullable Instant createdAt);

    abstract CacheMetadata autoBuild();

    /**
     * Builds the metadata.
     *
     * @throws IllegalStateException if a count is negative, or if only some of {@code cacheName},
     *     {@code expireTime} and {@code invocationsUsed} are set
     */
    public CacheMetadata build() {
      CacheMetadata metadata = autoBuild();
      checkState(metadata.contentsCount() >= 0, "contentsCount must not be negative.");
      checkState(
          metadata.invocationsUsed().orElse(0) >= 0, "invocationsUsed must not be negative.");
      boolean active = metadata.cacheName().isPresent();
      checkState(
          metadata.expireTime().isPresent() == active
              && metadata.invocationsUsed().isPresent() == active,
          "cacheName, expireTime and invocationsUsed must be all set or all unset.");
      return metadata;
    }
  }
}

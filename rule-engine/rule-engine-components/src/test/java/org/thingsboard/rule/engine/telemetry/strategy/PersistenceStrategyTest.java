/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.rule.engine.telemetry.strategy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceStrategyTest {

    @Test
    void testOnEveryMessageReturnsCorrectInstance() {
        PersistenceStrategy strategy = PersistenceStrategy.onEveryMessage();
        assertThat(strategy)
                .isNotNull()
                .isInstanceOf(OnEveryMessagePersistenceStrategy.class);
    }

    @Test
    void testDeduplicateReturnsCorrectInstance() {
        int validDeduplicationIntervalSecs = 5;
        PersistenceStrategy strategy = PersistenceStrategy.deduplicate(validDeduplicationIntervalSecs);
        assertThat(strategy)
                .isNotNull()
                .isInstanceOf(DeduplicatePersistenceStrategy.class);

        long actualDeduplicationIntervalMillis = (long) ReflectionTestUtils.getField(strategy, "deduplicationIntervalMillis");
        assertThat(actualDeduplicationIntervalMillis).isEqualTo(Duration.ofSeconds(validDeduplicationIntervalSecs).toMillis());
    }

    @Test
    void testSkipReturnsCorrectInstance() {
        PersistenceStrategy strategy = PersistenceStrategy.skip();
        assertThat(strategy)
                .isNotNull()
                .isInstanceOf(SkipPersistenceStrategy.class);
    }

}
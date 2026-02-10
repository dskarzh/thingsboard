/**
 * Copyright © 2016-2026 The Thingsboard Authors
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
package org.thingsboard.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThingsBoardThreadFactoryTest {

    @Test
    @DisplayName("newThread() should create thread with correct name prefix")
    void newThreadShouldCreateThreadWithCorrectNamePrefix() {
        // GIVEN
        ThingsBoardThreadFactory factory = ThingsBoardThreadFactory.forName("test-pool");

        // WHEN
        Thread thread = factory.newThread(() -> {});

        // THEN
        assertThat(thread.getName())
                .as("Thread name should start with the specified pool name")
                .startsWith("test-pool-");
    }

    @Test
    @DisplayName("newThread() should create non-daemon thread")
    void newThreadShouldCreateNonDaemonThread() {
        // GIVEN
        ThingsBoardThreadFactory factory = ThingsBoardThreadFactory.forName("test");

        // WHEN
        Thread thread = factory.newThread(() -> {});

        // THEN
        assertThat(thread.isDaemon())
                .as("Thread should not be a daemon thread")
                .isFalse();
    }

    @Test
    @DisplayName("newThread() should create thread with normal priority")
    void newThreadShouldCreateThreadWithNormalPriority() {
        // GIVEN
        ThingsBoardThreadFactory factory = ThingsBoardThreadFactory.forName("test");

        // WHEN
        Thread thread = factory.newThread(() -> {});

        // THEN
        assertThat(thread.getPriority())
                .as("Thread should have normal priority")
                .isEqualTo(Thread.NORM_PRIORITY);
    }

    @Test
    @DisplayName("newThread() should inherit thread group from current thread")
    void newThreadShouldInheritThreadGroup() {
        // GIVEN
        ThingsBoardThreadFactory factory = ThingsBoardThreadFactory.forName("test");

        // WHEN
        Thread thread = factory.newThread(() -> {});

        // THEN
        assertThat(thread.getThreadGroup())
                .as("Thread should have a thread group (inherited from current thread)")
                .isNotNull();
    }

    @Test
    @DisplayName("newThread() should create threads with incrementing numbers")
    void newThreadShouldCreateThreadsWithIncrementingNumbers() {
        // GIVEN
        ThingsBoardThreadFactory factory = ThingsBoardThreadFactory.forName("test");

        // WHEN
        Thread thread1 = factory.newThread(() -> {});
        Thread thread2 = factory.newThread(() -> {});
        Thread thread3 = factory.newThread(() -> {});

        // THEN
        assertThat(thread1.getName())
                .as("First thread should have thread number 1")
                .matches("test-\\d+-thread-1");
        assertThat(thread2.getName())
                .as("Second thread should have thread number 2")
                .matches("test-\\d+-thread-2");
        assertThat(thread3.getName())
                .as("Third thread should have thread number 3")
                .matches("test-\\d+-thread-3");
    }

    @Test
    @DisplayName("Different factories should have unique pool numbers")
    void differentFactoriesShouldHaveUniquePoolNumbers() {
        // GIVEN & WHEN
        ThingsBoardThreadFactory factory1 = ThingsBoardThreadFactory.forName("pool-a");
        ThingsBoardThreadFactory factory2 = ThingsBoardThreadFactory.forName("pool-b");
        Thread thread1 = factory1.newThread(() -> {});
        Thread thread2 = factory2.newThread(() -> {});

        // THEN
        assertThat(thread1.getName())
                .as("Thread from first factory should have pool-a in name")
                .startsWith("pool-a-");
        assertThat(thread2.getName())
                .as("Thread from second factory should have pool-b in name")
                .startsWith("pool-b-");
        assertThat(thread1.getName())
                .as("Threads from different factories should have different names")
                .isNotEqualTo(thread2.getName());
    }

    @Test
    @DisplayName("updateCurrentThreadName() should append topic to thread name")
    void updateCurrentThreadNameShouldAppendTopicToThreadName() {
        // GIVEN
        String originalName = Thread.currentThread().getName();

        // WHEN
        ThingsBoardThreadFactory.updateCurrentThreadName("topic-suffix");

        // THEN
        String updatedName = Thread.currentThread().getName();
        assertThat(updatedName)
                .as("Thread name should contain the topic separator and suffix")
                .contains(ThingsBoardThreadFactory.THREAD_TOPIC_SEPARATOR)
                .endsWith("topic-suffix");

        // CLEANUP
        Thread.currentThread().setName(originalName);
    }

    @Test
    @DisplayName("updateCurrentThreadName() should replace existing topic suffix")
    void updateCurrentThreadNameShouldReplaceExistingTopicSuffix() {
        // GIVEN
        String originalName = Thread.currentThread().getName();
        ThingsBoardThreadFactory.updateCurrentThreadName("first-topic");

        // WHEN
        ThingsBoardThreadFactory.updateCurrentThreadName("second-topic");

        // THEN
        String updatedName = Thread.currentThread().getName();
        assertThat(updatedName)
                .as("Thread name should have the new topic suffix")
                .endsWith("second-topic")
                .doesNotContain("first-topic");

        // CLEANUP
        Thread.currentThread().setName(originalName);
    }

    @Test
    @DisplayName("addThreadNamePrefix() should prepend prefix to thread name")
    void addThreadNamePrefixShouldPrependPrefixToThreadName() {
        // GIVEN
        String originalName = Thread.currentThread().getName();

        // WHEN
        ThingsBoardThreadFactory.addThreadNamePrefix("prefix");

        // THEN
        String updatedName = Thread.currentThread().getName();
        assertThat(updatedName)
                .as("Thread name should start with the prefix")
                .startsWith("prefix-");

        // CLEANUP
        Thread.currentThread().setName(originalName);
    }
}

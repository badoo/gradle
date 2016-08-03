/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.execution;

import org.gradle.StartParameter;
import org.gradle.api.GradleException;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.TaskOutputsInternal;
import org.gradle.api.internal.changedetection.TaskArtifactState;
import org.gradle.api.internal.tasks.TaskExecuter;
import org.gradle.api.internal.tasks.TaskExecutionContext;
import org.gradle.api.internal.tasks.TaskStateInternal;
import org.gradle.api.internal.tasks.cache.TaskCacheKey;
import org.gradle.api.internal.tasks.cache.TaskOutputCache;
import org.gradle.api.internal.tasks.cache.TaskOutputPacker;
import org.gradle.api.internal.tasks.cache.TaskOutputReader;
import org.gradle.api.internal.tasks.cache.TaskOutputWriter;
import org.gradle.api.internal.tasks.cache.config.TaskCachingInternal;
import org.gradle.util.Clock;
import org.gradle.util.SingleMessageLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkipCachedTaskExecuter implements TaskExecuter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkipCachedTaskExecuter.class);

    private final TaskCachingInternal taskCaching;
    private final StartParameter startParameter;
    private final TaskOutputPacker packer;
    private final TaskExecuter delegate;
    private TaskOutputCache cache;

    public SkipCachedTaskExecuter(TaskCachingInternal taskCaching, TaskOutputPacker packer, StartParameter startParameter, TaskExecuter delegate) {
        this.taskCaching = taskCaching;
        this.startParameter = startParameter;
        this.packer = packer;
        this.delegate = delegate;
        SingleMessageLogger.incubatingFeatureUsed("Task output caching");
    }

    @Override
    public void execute(TaskInternal task, TaskStateInternal state, TaskExecutionContext context) {
        Clock clock = new Clock();

        TaskOutputsInternal taskOutputs = task.getOutputs();
        boolean cacheAllowed = taskOutputs.isCacheAllowed();

        boolean shouldCache;
        try {
            shouldCache = cacheAllowed && taskOutputs.isCacheEnabled();
        } catch (Exception t) {
            throw new GradleException(String.format("Could not evaluate TaskOutputs.cacheIf for %s.", task), t);
        }

        LOGGER.debug("Determining if {} is cached already", task);

        TaskCacheKey cacheKey = null;
        if (shouldCache) {
            TaskArtifactState taskState = context.getTaskArtifactState();
            try {
                cacheKey = taskState.calculateCacheKey();
                LOGGER.debug("Cache key for {} is {}", task, cacheKey);
            } catch (Exception e) {
                throw new GradleException(String.format("Could not build cache key for %s.", task), e);
            }
        } else {
            if (!cacheAllowed) {
                LOGGER.debug("Not caching {} as it is not allowed", task);
            } else {
                LOGGER.debug("Not caching {} as task output is not cacheable.", task);
            }
        }

        if (cacheKey != null) {
            try {
                TaskOutputReader cachedOutput = getCache().get(cacheKey);
                if (cachedOutput != null) {
                    packer.unpack(taskOutputs, cachedOutput);
                    LOGGER.info("Unpacked output for {} from cache (took {}).", task, clock.getTime());
                    state.upToDate("FROM-CACHE");
                    return;
                }
            } catch (Exception e) {
                LOGGER.warn("Could not load cached output for {} with cache key {}", task, cacheKey, e);
            }
        }

        delegate.execute(task, state, context);

        if (cacheKey != null && state.getFailure() == null) {
            try {
                TaskOutputWriter cachedOutput = packer.createWriter(taskOutputs);
                getCache().put(cacheKey, cachedOutput);
            } catch (Exception e) {
                LOGGER.warn("Could not cache results for {} for cache key {}", task, cacheKey, e);
            }
        }
    }

    private TaskOutputCache getCache() {
        if (cache == null) {
            cache = taskCaching.getCacheFactory().createCache(startParameter);
            LOGGER.info("Using {}", cache.getDescription());
        }
        return cache;
    }
}

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

package org.gradle.internal.component.external.model;

import com.google.common.collect.Multimap;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.internal.component.external.descriptor.Artifact;
import org.gradle.internal.component.external.descriptor.MavenScope;
import org.gradle.internal.component.model.DefaultDependencyMetadata;
import org.gradle.internal.component.model.Exclude;

import java.util.List;

public class MavenDependencyMetadata extends DefaultDependencyMetadata {
    private final MavenScope scope;

    public MavenDependencyMetadata(MavenScope scope, ModuleVersionSelector requested, Multimap<String, String> confMappings, List<Artifact> artifacts, List<Exclude> excludes) {
        super(requested, requested.getVersion(), false, false, true, confMappings, artifacts, excludes);
        this.scope = scope;
    }

    public MavenScope getScope() {
        return scope;
    }
}

/*
 *    Copyright 2017 - 2018 BreadMoirai (Ton Ly)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.breadmoirai;

import org.gradle.api.internal.provider.AbstractProvider;
import org.gradle.api.provider.Provider;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class DebugProvider<T> extends AbstractProvider<T> {

    private final Logger log;
    private final String propertyName;
    private final Provider<T> provider;

    public DebugProvider(Logger log, String propertyName, Provider<T> provider) {
        this.log = log;
        this.propertyName = propertyName;
        this.provider = provider;
    }

    @Nullable
    @Override
    public Class<T> getType() {
        return null;
    }

    @Nullable
    @Override
    public T getOrNull() {
        T t = provider.get();
        log.debug(String.format("Provider['%s'] returned value '%s'", propertyName, t));
        return t;
    }
}

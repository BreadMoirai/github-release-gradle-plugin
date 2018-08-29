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
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;

public class CachedProvider<T> extends AbstractProvider<T> {

    final private Provider<T> provider;
    private boolean isset;
    private T value;

    public CachedProvider(Provider<T> provider) {
        this.provider = provider;
        this.isset = false;
    }

    @Nullable
    @Override
    public Class<T> getType() {
        if (provider instanceof ProviderInternal) {
            return ((ProviderInternal<T>) provider).getType();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public T getOrNull() {
        if (!isset) {
            value = provider.getOrNull();
            isset = true;
        }
        return value;
    }
}

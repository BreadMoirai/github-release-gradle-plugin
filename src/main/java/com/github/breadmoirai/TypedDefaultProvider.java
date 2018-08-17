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
import org.gradle.internal.UncheckedException;
import org.gradle.internal.impldep.org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.Callable;

public class TypedDefaultProvider<T> extends AbstractProvider<T> {

    private final Class<T> type;
    private final Callable<T> value;

    public TypedDefaultProvider(Class<T> type, Callable<T> value) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(type, "value");
        this.type = type;
        this.value = value;
    }

    @NotNull
    @Override
    public Class<T> getType() {
        return type;
    }

    @Nullable
    @Override
    public T getOrNull() {
        try {
            return value.call();
        } catch (Exception e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

}

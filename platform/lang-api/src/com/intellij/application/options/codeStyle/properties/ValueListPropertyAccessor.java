// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.application.options.codeStyle.properties;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public abstract class ValueListPropertyAccessor<T> extends CodeStylePropertyAccessor<T, List<String>> {
  public ValueListPropertyAccessor(@NotNull Object object, @NotNull Field field) {
    super(object, field);
  }

  @Nullable
  @Override
  protected abstract T fromExternal(@NotNull List<String> extVal);

  @NotNull
  @Override
  protected abstract List<String> toExternal(@NotNull T value);

  @Nullable
  @Override
  protected List<String> parseString(@NotNull String string) {
    return getValueList(string);
  }

  @NotNull
  protected static List<String> getValueList(@NotNull String string) {
    return ContainerUtil.map(string.split(","), s -> s.trim());
  }
}

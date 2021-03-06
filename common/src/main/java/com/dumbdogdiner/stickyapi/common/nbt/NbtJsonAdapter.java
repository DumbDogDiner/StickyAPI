/*
 * Copyright (c) 2020-2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.nbt;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

/**
 * Utility class for easy creation of the appropriate {@link NbtTag} from an existing {@link JsonElement}
 */
@UtilityClass
public final class NbtJsonAdapter {
    /**
     * Creates the appropriate tag from a given {@link JsonElement}, but never creates a {@link NbtJsonTag}
     * @param element The incoming {@link JsonElement} to convert
     * @return the new {@link NbtTag}
     */
    public static NbtTag fromJson(@NotNull JsonElement element) {
        if(element.isJsonArray()){
            return NbtListTag.fromJsonArray(element.getAsJsonArray());
        } else if(element.isJsonObject()){
            return NbtCompoundTag.fromJsonObject(element.getAsJsonObject());
        } else if (element.isJsonPrimitive()) {
            // TODO: Rewrite to look cleaner?
            // https://github.com/DumbDogDiner/StickyAPI/pull/92/files#r575639999
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if(primitive.isBoolean()){
                return NbtBooleanTag.fromPrimitive(primitive);
            } else if(primitive.isNumber()){
                return NbtNumberTag.fromPrimitive(primitive);
            } else if(primitive.isString()){
                return NbtStringTag.fromPrimitive(primitive);
            } else {
                throw new UnsupportedOperationException("Illegal type of NBT primitive");
            }
        } else if(element.isJsonNull()) {
            return null;
        } else {
            throw new UnsupportedOperationException(MessageFormat.format("This type of JSONElement is unsupported ({0})", element.getClass().getName()));
        }

    }
}

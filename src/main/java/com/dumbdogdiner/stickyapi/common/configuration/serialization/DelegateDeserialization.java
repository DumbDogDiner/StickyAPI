/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.configuration.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;

/**
 * Applies to a {@link ConfigurationSerializable} that will delegate all
 * deserialization to another {@link ConfigurationSerializable}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DelegateDeserialization {
    /**
     * Which class should be used as a delegate for this classes deserialization
     *
     * @return Delegate class
     */
    @NotNull
    public Class<? extends ConfigurationSerializable> value();
}

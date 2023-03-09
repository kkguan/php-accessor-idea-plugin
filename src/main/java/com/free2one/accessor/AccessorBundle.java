package com.free2one.accessor;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class AccessorBundle extends DynamicBundle {
    public static final @NonNls String BUNDLE = "messages.AccessorBundle";
    private static final AccessorBundle INSTANCE = new AccessorBundle();

    private AccessorBundle() {
        super("messages.AccessorBundle");
    }

    public static @NotNull @Nls String message(@NotNull @PropertyKey(
            resourceBundle = "messages.AccessorBundle"
    ) String key, @NotNull Object... params) {
        
        return INSTANCE.getMessage(key, params);
    }
}

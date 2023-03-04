package com.free2one.accessor.meta;

import com.free2one.accessor.method.GetterMethod;
import com.free2one.accessor.method.SetterMethod;

public class AccessorMethodDeserializerFactory {

    public static AccessorMethodDeserializer create() {
        AccessorMethodDeserializer accessorMethodDeserializer = new AccessorMethodDeserializer("name");
        accessorMethodDeserializer.registerSpecsType("getter", GetterMethod.class);
        accessorMethodDeserializer.registerSpecsType("setter", SetterMethod.class);

        return accessorMethodDeserializer;
    }
}

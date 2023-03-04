package com.free2one.accessor.meta;

import com.free2one.accessor.method.AccessorMethod;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AccessorMethodDeserializer implements JsonDeserializer<AccessorMethod> {

    String accessorMethodName;

    Gson gson;

    Map<String, Class<? extends AccessorMethod>> accessorMethodRegistry;
    
    public AccessorMethodDeserializer(String accessorMethodName) {
        this.accessorMethodName = accessorMethodName;
        gson = new Gson();
        accessorMethodRegistry = new HashMap<>();
    }

    public void registerSpecsType(String tslTypeName, Class<? extends AccessorMethod> tslSpecs) {
        accessorMethodRegistry.put(tslTypeName, tslSpecs);
    }

    @Override
    public AccessorMethod deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject dataObject = jsonElement.getAsJsonObject();
        JsonElement element = dataObject.get(accessorMethodName);
        Class<? extends AccessorMethod> accessorMethod = accessorMethodRegistry.get(element.getAsString());
        return gson.fromJson(dataObject, accessorMethod);
    }
}

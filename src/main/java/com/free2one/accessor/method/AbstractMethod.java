package com.free2one.accessor.method;

import java.util.Set;

public abstract class AbstractMethod implements AccessorMethod {

    protected String className;

    protected String fieldName;

    protected Set<String> fieldTypes;

    protected String methodName;


    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Set<String> getFieldTypes() {
        return fieldTypes;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }
    
}

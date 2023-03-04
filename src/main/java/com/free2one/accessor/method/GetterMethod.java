package com.free2one.accessor.method;

import com.intellij.openapi.util.text.StringUtil;

import java.util.HashSet;
import java.util.Set;

public class GetterMethod extends AbstractMethod {

    protected Set<String> returnTypes = new HashSet<>();

    protected String name = "getter";

    public GetterMethod(String className, String fieldName, Set<String> fieldTypes) {
        this.className = className;
        this.fieldName = fieldName;
        this.fieldTypes = fieldTypes;
        generateMethodName();
        generateReturnTypes();
    }

    private void generateMethodName() {
        methodName = "get" + StringUtil.wordsToBeginFromUpperCase(fieldName);
    }

    private void generateReturnTypes() {
        if (fieldTypes.isEmpty()) {
            returnTypes.add("void");
        } else {
            returnTypes.addAll(fieldTypes);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public String getText() {
        return "public function " + methodName + "() " + genReturnTypes2String() + " {}";
    }

    private String genReturnTypes2String() {
        StringBuilder types = new StringBuilder(":");
        this.returnTypes.forEach(t -> {
            if (types.length() != 1) {
                types.append("|");
            }

            types.append(t);
        });
        return types.toString();
    }

    public Set<String> getReturnTypes() {
        return returnTypes;
    }

}

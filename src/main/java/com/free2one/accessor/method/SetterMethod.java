package com.free2one.accessor.method;

import com.intellij.openapi.util.text.StringUtil;

import java.util.Set;

public class SetterMethod extends AbstractMethod {

    protected String name = "setter";

    public SetterMethod(String className, String fieldName, Set<String> fieldTypes) {
        this.className = className;
        this.fieldName = fieldName;
        this.fieldTypes = fieldTypes;
        generateMethodName();
    }

    private void generateMethodName() {
        this.methodName = "set" + StringUtil.wordsToBeginFromUpperCase(this.fieldName);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getText() {
        return "public function " + methodName + " (" + genParameterString() + ") : " + this.className + " {}";

    }

    private String genParameterString() {
        StringBuilder parameters = new StringBuilder();
        int num = 0;
        for (String fieldType : fieldTypes
        ) {
            ++num;
            if (parameters.length() != 1) {
                parameters.append("|");
            }
            parameters.append(fieldType);
            if (parameters.length() == num) {
                parameters.append(" ");
            }
        }
        parameters.append(this.fieldName);

        return parameters.toString();
    }

}

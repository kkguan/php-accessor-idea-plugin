package com.free2one.accessor.meta;

import com.free2one.accessor.method.AccessorMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class ClassMetadata {

    private String classname;

    private String accessorClassname;

    private String project;

    private ArrayList<AccessorMethod> methods;

    public ClassMetadata() {
    }

    public ClassMetadata(String project, String classname) {
        this.project = project;
        this.classname = classname;
        this.methods = new ArrayList<>();
    }


    public String getProject() {
        return project;
    }

    public String getClassname() {
        return classname;
    }

    public String getAccessorClassname() {
        return accessorClassname;
    }

    public ClassMetadata addMethod(AccessorMethod method) {
        methods.add(method);
        return this;
    }

    public ArrayList<AccessorMethod> getMethods() {
        return methods;
    }

    public String findFieldNameFromMethodName(String methodName) {
        for (AccessorMethod method : methods) {
            if (!method.getMethodName().equals(methodName)) {
                continue;
            }

            return method.getFieldName();
        }

        return null;
    }

    public Collection<String> findMethodNamesFromFieldName(String fieldName) {
        return methods.stream()
                .filter(method -> method.getFieldName().equals(fieldName))
                .map(AccessorMethod::getMethodName)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}

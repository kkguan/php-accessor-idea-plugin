package com.free2one.accessor.method;

import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;

import java.util.HashMap;
import java.util.Map;

public class MethodFactory {

    public static Map<String, AccessorMethod> createFromField(Field field) {
        //不处理常量
        if (field instanceof ClassConstImpl) {
            return null;
        }
        PhpClass containClass = field.getContainingClass();
        AccessorMethod getterMethod = new GetterMethod(containClass.getName(), field.getName(), field.getType().getTypes());
        AccessorMethod setterMethod = new SetterMethod(containClass.getName(), field.getName(), field.getType().getTypes());

        return new HashMap<>() {
            {
                put(getterMethod.getMethodName(), getterMethod);
                put(setterMethod.getMethodName(), setterMethod);
            }
        };
    }

}

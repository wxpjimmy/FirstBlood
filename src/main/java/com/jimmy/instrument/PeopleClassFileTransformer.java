package com.jimmy.instrument;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by wangxiaopeng on 2017/4/21.
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 */
public class PeopleClassFileTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("loader:"+ loader.getClass().getName());
        System.out.println("load class:"+className);

        if("com.jimmy.instrument.People".equals(className)) {
            try {
                //通过javassist修改sayHello方法字节码

                CtClass ctClass= ClassPool.getDefault().get(className.replace('/','.'));

                CtMethod sayHelloMethod=ctClass.getDeclaredMethod("sayHello");

                sayHelloMethod.insertBefore("System.out.println(\"before sayHello----\");");

                sayHelloMethod.insertAfter("System.out.println(\"after sayHello----\");");

                return ctClass.toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return classfileBuffer;
    }
}

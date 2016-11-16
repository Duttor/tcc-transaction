package io.anyway.galaxy.util;

import io.anyway.galaxy.proxy.ProxyFactory;
import io.anyway.galaxy.proxy.TXOperationProxy;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Created by xiong.j on 2016/7/25.
 */
public class ProxyUtil {

    /**
     * 动态代理方法调用
     *
     * @param aopBean 目标代理对象
     * @param targetClass 目标类定义
     * @param methodName 方法名
     * @param types 参数类型
     * @param args 参数值
     * @return Object
     * @throws Throwable
     *
     */
    public static Object proxyMethod(Object aopBean, Class<?> targetClass, String methodName, Class<?>[] types, Object[] args) throws Throwable {
        Method method = ReflectionUtils.findMethod(targetClass,methodName,types);
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method,aopBean,args);
    }

    /**
     * 静态代理方法调用TXOperationProxy.invokeCancel
     *
     * @param className 类名(Try方法名)
     * @param target 目标对象
     * @param types 参数类型
     * @param targetMethod 被代理代理方法
     * @param args 参数值
     */
    public static void invokeCancel(String className, Object target, Class<?>[] types, String[] targetMethod, Object[] args) throws Throwable {
        TXOperationProxy txOperationProxy = ProxyFactory.getProxy(className, target, TXOperationProxy.class, targetMethod, types);
        txOperationProxy.invokeCancel(target, args);
    }

    /**
     * 静态代理方法调用TXOperationProxy.invokeConfirm
     *
     * @param className 类名(Try方法名)
     * @param target 目标对象
     * @param types 参数类型
     * @param targetMethod 被代理代理方法
     * @param args 参数值
     * @throws Throwable
     */
    public static void invokeConfirm(String className, Object target, Class<?>[] types, String[] targetMethod, Object[] args) throws Throwable {
        TXOperationProxy txOperationProxy = ProxyFactory.getProxy(className, target, TXOperationProxy.class, targetMethod, types);
        txOperationProxy.invokeConfirm(target, args);
    }
}

package io.anyway.galaxy.proxy;

/**
 * Created by xiong.j on 2016/7/27.
 */
public interface TXOperationProxy {

    void invokeConfirm(Object target, Object[] args) throws Throwable;

    void invokeCancel(Object target, Object[] args) throws Throwable;
}

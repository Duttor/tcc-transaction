package io.anyway.galaxy.intercepter;

import io.anyway.galaxy.common.TransactionTypeEnum;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.support.ActionExecutePayload;

import java.sql.Connection;

/**
 * Created by yangzz on 16/7/20.
 */
public interface ActionIntercepter {

    /**
     * 在开启业务事务时先记录一条TX记录,状态为trying
     * 这个过程开启新的事务,成功返回唯一的事务编号,失败抛异常
     * @param serialNumber 业务流水号
     * @param payload Action执行体定义
     * @return 事务上下文包含事务标识和业务流水号
     */
    TXContext addAction(String serialNumber,ActionExecutePayload payload) throws Throwable;

    /**
     * 尝试成功更新,更新事务状态为tried
     * @param ctx 分布式上下文,包括事务编号和业务流水编号
     */
    void tryAction(TXContext ctx) throws Throwable;
    /**
     * 更新事务状态为confirmed
     * @param ctx 分布式上下文,包括事务编号和业务流水编号
     */
    void confirmAction(TXContext ctx) throws Throwable;

    /**
     * 更新事务状态为cancelled
     * @param ctx 分布式上下文,包括事务编号和业务流水编号
     */
    void cancelAction(TXContext ctx) throws Throwable;

}

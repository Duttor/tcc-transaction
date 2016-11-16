package io.anyway.galaxy.intercepter;

import java.sql.Connection;

import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.support.ServiceExecutePayload;

/**
 * Created by yangzz on 16/7/21.
 */
public interface ServiceIntercepter {

    /**
     * 尝试执行业务事务,事务执行方法的入口
     * @param ctx 分布式上下文,包括事务编号和业务流水编号
     * @param payload 存储的Service执行体内容,包含了try/confirm/cancel等方法定义
     */
    void tryService(TXContext ctx,ServiceExecutePayload payload)throws Throwable;

    /**
     * 提交事务,努力送达型通过调度任务实现持久化成功
     * @param ctx 分布式上下文,包括事务编号和业务流水编号
     */
    void confirmService(TXContext ctx)throws Throwable;

    /**
     * 回滚事务,尝试若干次回顾使用调度任务完成,如果失败交由人工处理
     * @param ctx 分布式上下文,包括事务编号和业务流水编号
     */
    void cancelService(TXContext ctx)throws Throwable;
}

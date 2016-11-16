package io.anyway.galaxy.message;

import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.domain.TransactionInfo;

/**
 * Created by yangzz on 16/7/27.
 */
public interface TransactionMessageService{

    /**
     * 同步发送消息
     * @param ctx
     * @param txStatus
     * @throws Throwable
     */
    public void sendMessage(TXContext ctx, TransactionStatusEnum txStatus) throws Throwable;

    /**
     * 判断消息是否已处理，如未处理则保存消息
     * @param message
     * @return true 未处理, false 已处理
     * @throws Throwable
     */
    boolean isValidMessage(TransactionMessage message) throws Throwable;

    /**
     * 处理消息
     * @param message
     */
    public void handleMessage(TransactionMessage message) throws Throwable;

    /**
     * 异步方式处理消息
     * @param message
     */
    public void asyncHandleMessage(TransactionMessage message);

}

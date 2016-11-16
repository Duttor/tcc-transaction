package io.anyway.galaxy.message.producer;

/**
 * 消息生产者接口
 *
 * Created by xiong.j on 2016/7/21.
 */
public interface MessageProducer<T> {
    /**
     * 同步发送消息
     *
     * @param message 消息Model
     */
    public void sendMessage(T message) throws Throwable;
}

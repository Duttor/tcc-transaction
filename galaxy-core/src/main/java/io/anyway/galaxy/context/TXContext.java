package io.anyway.galaxy.context;

import java.util.Date;

/**
 * Created by yangzz on 16/7/21.
 */
public interface TXContext extends SerialNumberGenerator {
    /**
     * 获取全局事务标识
     * @return
     */
    long getParentId();

    /**
     * 获取子事务单元事务标识
     * @return
     */
    long getTxId();

    /**
     * 获取业务类型
     * @return
     */
    String getBusinessType();

    /**
     * 获取事务类型
     * @return
     */
    int getTxType();

    /**
     * 超时时间
     * @return
     */
    long getTimeout();

    /**
     *
     */
    Date getCallTime();
}


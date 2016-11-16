package io.anyway.galaxy.context;

import java.io.Serializable;

/**
 * Created by yangzz on 16/7/28.
 */
public interface SerialNumberGenerator extends Serializable {
    /**
     * 获取交易流水号
     * @return
     */
    String getSerialNumber();
}

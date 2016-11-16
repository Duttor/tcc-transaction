package io.anyway.galaxy.demo.service;

import io.anyway.galaxy.context.SerialNumberGenerator;

/**
 * Created by yangzz on 16/7/19.
 */
public interface PurchaseService {
    /**
     * 购买理财产品
     * @param generator 业务流水号生成器
     * @param userId 用户标识
     * @param productId 商品ID
     * @param amount 购买数量
     * @param tcase 测试类别
     * @return
     * @throws Exception
     */
    String purchase(SerialNumberGenerator generator, long userId, long productId, long amount, int tcase);
}

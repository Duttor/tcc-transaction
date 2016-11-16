package io.anyway.galaxy.demo.service;

import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.demo.domain.OrderDO;

/**
 * Created by yangzz on 16/7/19.
 */
public interface OrderService {
    /**
     * 增加订单操作
     * @param ctx 事务上下文
     * @param orderDO 订单对象
     * @return
     */
    boolean addOrder(TXContext ctx,OrderDO orderDO);

    boolean cancelOrder(TXContext ctx,OrderDO orderDO);
}

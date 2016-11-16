package io.anyway.galaxy.demo.dao;

import io.anyway.galaxy.demo.domain.OrderDO;

/**
 * Created by yangzz on 16/7/19.
 */
public interface OrderDao {

    /**
     * 增加订单,订单的id需要程序中给出,不要自动生成
     * @param orderDO
     * @return
     */
    int addOrder(OrderDO orderDO);

    /**
     * 删除订单
     * @param orderDO
     * @return
     */
    int deleteOrder(OrderDO orderDO);
};

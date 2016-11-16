package io.anyway.galaxy.demo.service.impl;

import io.anyway.galaxy.annotation.TXCancel;
import io.anyway.galaxy.annotation.TXTry;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.demo.dao.OrderDao;
import io.anyway.galaxy.demo.domain.OrderDO;
import io.anyway.galaxy.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by yangzz on 16/7/19.
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao dao;

    @Override
    @Transactional
    @TXTry(cancel = "cancelOrder")
    public boolean addOrder(TXContext ctx,OrderDO orderDO) {
        return 0< dao.addOrder(orderDO);
    }

    @Override
    @Transactional
    @TXCancel
    public boolean cancelOrder(TXContext ctx,OrderDO orderDO){
        return 0 < dao.deleteOrder(orderDO);
    }
}

package io.anyway.galaxy.demo.rest;

import com.google.common.base.Strings;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.support.TXContextSupport;
import io.anyway.galaxy.demo.domain.OrderDO;
import io.anyway.galaxy.demo.service.OrderService;
import io.anyway.galaxy.exception.DistributedTransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 下单
 * @author xiong.jie
 * @version $Id: OrderRest.java, v 0.1 2016-7-20 下午1:33:31 Exp $
 */
@Controller
@RequestMapping(value = "/rest")
public class OrderRest {
    /** 下单处理Service */
    @Autowired
    private OrderService orderService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public boolean purchase(@RequestBody Map<String,Object> params)throws Exception {
        TXContextSupport tx = null;
        if (params.get("txId") !=null) {
            int tcase = (Integer) (params.get("tcase"));
            switch (tcase) {
                case 2:  // 模拟下单失败
                    throw new DistributedTransactionException("Test repository failed!!");
                case 4:  // 模拟下单超时
                    params.put("timeout", 1L);
                    Thread.sleep(1000);
            }
            long txId= Long.parseLong(params.get("txId").toString());
            int txType = (Integer)(params.get("txType"));
            String businessType = (String)params.get("businessType");
            String serialNumber= (String)params.get("serialNumber");
            Date callTime = null;
            if (params.get("callTime") != null) {
                callTime = new Date((Long)params.get("callTime"));
            }
            long timeout = -1L;
            if (params.get("timeout") != null) {
                timeout = Long.parseLong(params.get("timeout").toString());
            }
            tx= new TXContextSupport(txId, serialNumber, businessType);
            tx.setTimeout(timeout);
            tx.setCallTime(callTime);
        }

        long orderId= nextval();
        long productId= Long.parseLong(params.get("productId").toString());
        long userId= Long.parseLong(params.get("userId").toString());
        String status= "success";
        long amount= Long.parseLong(params.get("amount").toString());
    	OrderDO orderDO= new OrderDO(orderId,productId,userId,status,amount);

    	return orderService.addOrder(tx,orderDO);
    }

    private synchronized long nextval() {
        Long value= 1L;
        File file= new File(System.getProperty("user.dir"),"order.sequence.id");
        if(file.exists()){
            try {
                byte[] bb= new byte[64];
                InputStream in= new FileInputStream(file);
                int available= in.read(bb);
                value= Long.parseLong(new String(bb,0,available));
                value++;
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            OutputStream out= new FileOutputStream(file);
            out.write(value.toString().getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}
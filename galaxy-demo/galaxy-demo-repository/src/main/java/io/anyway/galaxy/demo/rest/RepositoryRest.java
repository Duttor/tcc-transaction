package io.anyway.galaxy.demo.rest;

import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.support.TXContextSupport;
import io.anyway.galaxy.demo.service.RepositoryService;
import io.anyway.galaxy.exception.DistributedTransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Map;

/**
 * 下单
 * @author xiong.jie
 * @version $Id: RepositoryRest.java, v 0.1 2016-7-20 下午1:33:31 Exp $
 */
@Controller
@RequestMapping(value = "/rest")
public class RepositoryRest {
    /** 库存Service */
    @Autowired
    private RepositoryService repositoryService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public boolean purchase(@RequestBody  Map<String,Object> params)throws Exception {
        TXContextSupport tx = null;
        if (params.get("txId") !=null) {
            int tcase = (Integer) (params.get("tcase"));
            switch (tcase) {
                case 1:  // 模拟减库存失败
                    throw new DistributedTransactionException("Test repository failed!!");
                case 3:  // 模拟减库存超时
                    params.put("timeout", 1L);
                    Thread.sleep(1000);
            }
            long txId = Long.parseLong(params.get("txId").toString());
            int txType = (Integer) (params.get("txType"));
            String businessType = (String) params.get("businessType");
            String serialNumber = (String) params.get("serialNumber");
            Date callTime = null;
            if (params.get("callTime") != null) {
                callTime = new Date((Long) params.get("callTime"));
            }
            long timeout = -1L;
            if (params.get("timeout") != null) {
                timeout = Long.parseLong(params.get("timeout").toString());
            }
            tx = new TXContextSupport(txId, serialNumber, businessType);
            tx.setTimeout(timeout);
            tx.setCallTime(callTime);


        }

        long productId= Long.parseLong(params.get("productId").toString());
        long amount= Long.parseLong(params.get("amount").toString());
    	return repositoryService.decreaseRepository(tx,productId,amount);
    }

}
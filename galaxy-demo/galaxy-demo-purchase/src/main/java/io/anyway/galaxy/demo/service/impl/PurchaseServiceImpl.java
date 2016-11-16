package io.anyway.galaxy.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.galaxy.annotation.TXAction;
import io.anyway.galaxy.common.TransactionTypeEnum;
import io.anyway.galaxy.context.SerialNumberGenerator;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.TXContextHolder;
import io.anyway.galaxy.demo.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangzz on 16/7/19.
 */

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private AtomicInteger oId = new AtomicInteger(1);

    @Value("${rest.repository.url}")
    private String repositoryURL;

    @Value("${rest.order.url}")
    private String orderURL;

    @Autowired
    private RestOperations restOperations;

    @Override
    @Transactional
    @TXAction(value = TransactionTypeEnum.TC,bizType = "purchase")
    public String purchase(SerialNumberGenerator generator, long userId, long productId, long amount, int tcase){

        TXContext ctx= TXContextHolder.getTXContext();

        final JSONObject request= new JSONObject();
        if (ctx != null) {
            request.put("txId",ctx.getTxId());
            request.put("txType",ctx.getTxType());
            request.put("timeout",30 * 1000);
            request.put("callTime",System.currentTimeMillis());
            request.put("businessType",ctx.getBusinessType());
            request.put("serialNumber",ctx.getSerialNumber());
        }
        request.put("productId",productId);
        request.put("amount",amount);
        request.put("userId",userId);
        request.put("tcase", tcase);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(request, headers);

        ResponseEntity<Boolean> result= restOperations.exchange(repositoryURL, HttpMethod.POST,entity, Boolean.class);
        if(result.getBody()){
            result= restOperations.exchange(orderURL, HttpMethod.POST,entity, Boolean.class);
            if(result.getBody()){
                return "购买产品成功";
                //throw new RuntimeException("测试回滚");
            }
            throw new RuntimeException("生成订单操作失败.");
        }
        else{
            throw new RuntimeException("减库存操作失败.");
        }
    }


}

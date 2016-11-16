package io.anyway.galaxy.demo.rest;

import io.anyway.galaxy.context.SerialNumberGenerator;
import io.anyway.galaxy.demo.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;

/**
 * 下单
 * @author xiong.jie
 * @version $Id: PurchaseRest.java, v 0.1 2016-7-20 下午1:33:31 Exp $
 */
@Controller
@RequestMapping(value = "/rest")
public class PurchaseRest {
    /** 下单处理Service */
    @Autowired
    private PurchaseService purchaseService;

    @RequestMapping(value="/{amount}")
    @ResponseBody
    public String purchase(@PathVariable long amount)throws Exception {

        SerialNumberGenerator generator= new SerialNumberGenerator() {
            @Override
            public String getSerialNumber() {
                return "serial_"+new Random ().nextInt(100000);
            }
        };

        long userId = 1;
        long productId = 1;
        int tcase = 0; // 正常

        String result=  purchaseService.purchase(generator,userId,productId,amount, tcase);
        return result;
    }

    @RequestMapping(value="case/{tcase}")
    @ResponseBody
    public String purchaseCase(@PathVariable int tcase)throws Exception {

        SerialNumberGenerator generator= new SerialNumberGenerator() {
            @Override
            public String getSerialNumber() {
                return "serial_"+new Random ().nextInt(100000);
            }
        };

        long userId = 1;
        long productId = 1;
        long amount = 1;

        String result=  purchaseService.purchase(generator,userId,productId,amount, tcase);
        return result;
    }

}
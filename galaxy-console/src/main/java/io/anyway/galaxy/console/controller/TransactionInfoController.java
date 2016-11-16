package io.anyway.galaxy.console.controller;

import io.anyway.galaxy.console.domain.DataSourceInfo;
import io.anyway.galaxy.console.domain.TransactionInfo;
import io.anyway.galaxy.console.service.TransactionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/8.
 */
@Controller
@RequestMapping("transactionInfo")
public class TransactionInfoController {

    @Autowired
    TransactionInfoService transactionInfoService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<TransactionInfo> listTransactionInfo(TransactionInfo transactionInfo) {
        return transactionInfoService.list(transactionInfo);
    }
    
}

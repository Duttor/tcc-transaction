package io.anyway.galaxy.console.controller;

import io.anyway.galaxy.console.dal.dao.BusinessTypeDao;
import io.anyway.galaxy.console.dal.dao.DataSourceInfoDao;
import io.anyway.galaxy.console.dal.db.DsTypeContextHolder;
import io.anyway.galaxy.console.dal.dto.BusinessTypeDto;
import io.anyway.galaxy.console.dal.dto.DataSourceInfoDto;
import io.anyway.galaxy.console.dal.dto.TransactionInfoDto;
import io.anyway.galaxy.console.dal.rdao.TransactionInfoDao;
import io.anyway.galaxy.console.domain.BusinessTypeInfo;
import io.anyway.galaxy.console.domain.TransactionInfo;
import io.anyway.galaxy.console.service.TransactionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.crypto.Data;
import java.util.List;

/**
 * Created by xiong.j on 2016/8/2.
 */
@Controller
@RequestMapping("/jsp")
public class TestController {

    @Autowired
    private TransactionInfoDao transactionInfoDao;

    @Autowired
    private DataSourceInfoDao dataSourceInfoDao;

    @Autowired
    private BusinessTypeDao businessTypeDao;

    @Autowired
    TransactionInfoService transactionInfoService;

    @RequestMapping(value="/testAysnJms/{times}")
    @ResponseBody
    public String testAysnJms(Model model, @PathVariable int times) {
//        long start = System.currentTimeMillis();
//        long end = System.currentTimeMillis() - start;
        TransactionInfoDto transactionInfoDto = null;

        System.out.println("dataSourceInfoDao:" + dataSourceInfoDao.get(1));

        DataSourceInfoDto dsInfo = dataSourceInfoDao.get(1);
        DsTypeContextHolder.setContextType(DsTypeContextHolder.DYNAMIC_SESSION_FACTORY);

        DsTypeContextHolder.setDsInfo(dsInfo);

        transactionInfoDto = transactionInfoDao.get(1);
        return "Test transactionInfoDao result|" + transactionInfoDto;
    }

    @RequestMapping(value="/traninfo/{id}")
    @ResponseBody
    public String getTransactionInfo(Model model, @PathVariable int id) {
        long start = System.currentTimeMillis();
        TransactionInfoDto transactionInfoDto = null;

        BusinessTypeInfo businessTypeInfo = new BusinessTypeInfo();
        businessTypeInfo.setId(1);
        businessTypeInfo.setName("test");

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setBusinessType(businessTypeInfo.getName());
        List<TransactionInfo> result = transactionInfoService.list(transactionInfo);

        long end = System.currentTimeMillis() - start;
        System.out.println("@@@@TestController.getTransactionInfo Spent time" + end);
        return "Test transactionInfos result|" + result;
    }

    @RequestMapping(value="/busin/{id}")
    @ResponseBody
    public String getBusinessTypeInfo(Model model, @PathVariable int id) {
        long start = System.currentTimeMillis();

        BusinessTypeDto result = new BusinessTypeDto();
        result.setId(1);
        //DsTypeContextHolder.setContextType(DsTypeContextHolder.DEFAULT_SESSION_FACTORY);
        result = businessTypeDao.get(1);
        businessTypeDao.list(result);
        return "Test businessTypeInfo result|" + result;
    }
}

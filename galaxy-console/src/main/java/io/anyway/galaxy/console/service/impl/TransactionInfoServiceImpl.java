package io.anyway.galaxy.console.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.anyway.galaxy.console.common.DataSourceStatusEnum;
import io.anyway.galaxy.console.dal.dao.BusinessTypeDao;
import io.anyway.galaxy.console.dal.dao.DataSourceInfoDao;
import io.anyway.galaxy.console.dal.db.DsTypeContextHolder;
import io.anyway.galaxy.console.dal.dto.BusinessTypeDto;
import io.anyway.galaxy.console.dal.dto.DataSourceInfoDto;
import io.anyway.galaxy.console.dal.dto.TransactionInfoDto;
import io.anyway.galaxy.console.dal.rdao.TransactionInfoDao;
import io.anyway.galaxy.console.domain.TransactionInfo;
import io.anyway.galaxy.console.protocol.HttpClientService;
import io.anyway.galaxy.console.service.TransactionInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiong.j on 2016/8/4.
 */
@Slf4j
@Service
public class TransactionInfoServiceImpl implements TransactionInfoService {

    @Autowired
    private ThreadPoolTaskExecutor multTaskExecutor;

    @Autowired
    private BusinessTypeDao businessTypeDao;

    @Autowired
    private TransactionInfoDao transactionInfoDao;

    @Autowired
    private DataSourceInfoDao dataSourceInfoDao;

    @Autowired
    private HttpClientService httpClientService;

    private static long WAIT_TIME = 30;

    public List<TransactionInfo> list(final TransactionInfo transactionInfo) {
        BusinessTypeDto businessTypeDto;

        // 获取业务状态详情
        if (transactionInfo.getBusinessTypeId() > 0L) {
            businessTypeDto = businessTypeDao.get(transactionInfo.getBusinessTypeId());
        } else {
            businessTypeDto = businessTypeDao.getByName(transactionInfo.getBusinessType());
        }

        if (businessTypeDto == null) return null;

        // 获取业务状态对应的数据源ID
        List<Future<TransactionInfoInner>> futureList = new ArrayList<Future<TransactionInfoInner>>();
        List<Long> dsIds = JSON.parseArray(businessTypeDto.getDsId(), Long.TYPE);

        // 获取数据源信息列表
        DataSourceInfoDto dataSourceInfoDto = new DataSourceInfoDto();
        dataSourceInfoDto.setIds(dsIds);
        List<DataSourceInfoDto> dataSourceInfoDtos = dataSourceInfoDao.list(dataSourceInfoDto);

        // 根据数据源信息列表获取事务信息
        for (final DataSourceInfoDto dsInfo : dataSourceInfoDtos) {
            Future<TransactionInfoInner> future = multTaskExecutor.submit(
                    new FindTransactionInfo(dsInfo, transactionInfo));

            futureList.add(future);
        }

        return getResult(futureList, businessTypeDto);
    }

    private List<TransactionInfo> getResult(List<Future<TransactionInfoInner>> futureList, BusinessTypeDto businessTypeDto){
        List<TransactionInfo> resultList = new ArrayList<TransactionInfo>();
        TransactionInfoInner transactionInfoInner = null;
        for (Future<TransactionInfoInner> future : futureList) {
            try {
                transactionInfoInner = future.get(WAIT_TIME, TimeUnit.SECONDS);
                if (transactionInfoInner != null) {
                    resultList.addAll(transactionInfoInner.getTransactionInfos());
                }
            } catch (Exception e) {
                if (transactionInfoInner != null) {
                    log.warn("Error, Get TransactionInfo from BusinessType=" + businessTypeDto.getName()
                            + ", dateSource id= " + transactionInfoInner.getDsId(), e);
                } else {
                    log.warn("Error, Get TransactionInfo from BusinessType=" + businessTypeDto.getName(), e);
                }
            }
        }
        return resultList;
    }

    private class FindTransactionInfo implements Callable<TransactionInfoInner>{

        private DataSourceInfoDto dataSourceInfoDto;

        private TransactionInfo transactionInfo;
        
        private TransactionInfoInner transactionInfoInner;

        public FindTransactionInfo(DataSourceInfoDto dsInfo, TransactionInfo transactionInfo){
            this.dataSourceInfoDto = dsInfo;
            this.transactionInfo = transactionInfo;
            transactionInfoInner = new TransactionInfoInner();
        }

        @Override
        public TransactionInfoInner call() throws Exception {
            List<TransactionInfo> infos;

            transactionInfoInner.setBusinessType(transactionInfo.getBusinessType());
            transactionInfoInner.setDsId(dataSourceInfoDto.getId());

            List<TransactionInfoDto> dtos = getTransactionInfo(dataSourceInfoDto);
            if (dtos != null) {
                infos = new ArrayList<TransactionInfo>();
                TransactionInfo transactionInfo;
                for (TransactionInfoDto dto : dtos) {
                    transactionInfo =  new TransactionInfo();
                    BeanUtils.copyProperties(dto, transactionInfo);
                    infos.add(transactionInfo);
                }
                transactionInfoInner.setTransactionInfos(infos);
            }
            return transactionInfoInner;
        }

        private List<TransactionInfoDto> getTransactionInfo(DataSourceInfoDto dataSourceInfoDto) throws Exception{
            if (dataSourceInfoDto.getStatus() == DataSourceStatusEnum.DB.getCode()) {
            	transactionInfoInner.setUrl(Strings.isNullOrEmpty(dataSourceInfoDto.getDbUrl()) ? dataSourceInfoDto.getDbUrl():dataSourceInfoDto.getJndi());
                return fromDb(dataSourceInfoDto);
            } else {
            	transactionInfoInner.setUrl(dataSourceInfoDto.getUrl());
                return fromProtocol(dataSourceInfoDto);
            }
        }

        private List<TransactionInfoDto> fromDb(DataSourceInfoDto dataSourceInfoDto) {
            // 设置线程上下文,使用配置的数据源查询事务信息
            DsTypeContextHolder.setContextType(DsTypeContextHolder.DYNAMIC_SESSION_FACTORY);
            DsTypeContextHolder.setDsInfo(dataSourceInfoDto);
            TransactionInfoDto transactionInfoDto = new TransactionInfoDto();
            BeanUtils.copyProperties(transactionInfo, transactionInfoDto);
            return transactionInfoDao.list(transactionInfoDto);
        }

        private List<TransactionInfoDto> fromProtocol(DataSourceInfoDto dataSourceInfoDto) throws Exception{
            List<TransactionInfoDto> resultList = null;
            // Http
            String result = null;
            if (dataSourceInfoDto.getStatus() == DataSourceStatusEnum.HTTP.getCode()) {
                Map<String, String> params = new HashMap<String, String>();
                if (transactionInfo.getTxId() > 0) {
                    params.put("txId", String.valueOf(transactionInfo.getTxId()));
                }
                if (transactionInfo.getParentId() > 0) {
                    params.put("parentId", String.valueOf(transactionInfo.getParentId()));
                }
                if (!Strings.isNullOrEmpty(transactionInfo.getModuleId())) {
                    params.put("moduleId", transactionInfo.getModuleId());
                }
                if (!Strings.isNullOrEmpty(transactionInfo.getBusinessId())) {
                    params.put("businessId", transactionInfo.getBusinessId());
                }
                if (!Strings.isNullOrEmpty(transactionInfo.getBusinessType())) {
                    params.put("businessType", transactionInfo.getBusinessType());
                }
                if (transactionInfo.getTxType() > -1) {
                    params.put("txType", String.valueOf(transactionInfo.getTxType()));
                }
                if (transactionInfo.getTxStatus() > -1) {
                    params.put("txStatus", String.valueOf(transactionInfo.getTxStatus()));
                }
                if (transactionInfo.getGmtCreated() != null) {
                    params.put("gmtCreated", String.valueOf(transactionInfo.getGmtCreated()));
                }
                result = httpClientService.doGet(dataSourceInfoDto.getUrl(), params);
            }
            resultList = JSON.parseObject(result, List.class);
            return resultList;
        }

    }

    private class TransactionInfoInner{

        private List<TransactionInfo> transactionInfos;

        private String businessType;

        private long dsId;

        private String url;

        public List<TransactionInfo> getTransactionInfos() {
           return transactionInfos;
        }

        public void setTransactionInfos(List<TransactionInfo> transactionInfos) {
           this.transactionInfos = transactionInfos;
        }

        public String getBusinessType() {
           return businessType;
        }

        public void setBusinessType(String businessType) {
           this.businessType = businessType;
        }

        public long getDsId() {
           return dsId;
        }

        public void setDsId(long dsId) {
           this.dsId = dsId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static void main(String args[]){
        String list = JSON.toJSONString(new long[]{0, 1, 2});
        System.out.println(list);
        JSON.parseArray(list, Long.TYPE);
    }
}

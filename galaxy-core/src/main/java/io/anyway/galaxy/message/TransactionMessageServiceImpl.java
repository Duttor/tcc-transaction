package io.anyway.galaxy.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import io.anyway.galaxy.common.Constants;
import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.common.TransactionTypeEnum;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.TXContextHolder;
import io.anyway.galaxy.context.support.ServiceExecutePayload;
import io.anyway.galaxy.context.support.TXContextSupport;
import io.anyway.galaxy.domain.RetryCount;
import io.anyway.galaxy.domain.TransactionInfo;
import io.anyway.galaxy.exception.DistributedTransactionException;
import io.anyway.galaxy.message.producer.MessageProducer;
import io.anyway.galaxy.repository.TransactionRepository;
import io.anyway.galaxy.spring.SpringContextUtil;
import io.anyway.galaxy.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiong.j on 2016/7/28.
 */
@Component
@Slf4j
public class TransactionMessageServiceImpl implements TransactionMessageService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MessageProducer<TransactionMessage> messageProducer;

    @Autowired
    private ThreadPoolTaskExecutor txMsgTaskExecutor;

    @Autowired
    private SpringContextUtil springContextUtil;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendMessage(final TXContext ctx, TransactionStatusEnum txStatus) throws Throwable {
        TransactionInfo lockInfo = new TransactionInfo();
        try {
            //对需处理的数据加锁
            lockInfo.setParentId(ctx.getParentId());
            lockInfo.setTxId(ctx.getTxId());
            lockInfo = transactionRepository.lock(lockInfo).get(0);
            if (lockInfo == null) return;

            //先发送消息,如果发送失败会抛出Runtime异常
            TransactionMessage message = new TransactionMessage();
            message.setParentId(ctx.getTxId());
            message.setBusinessId(ctx.getSerialNumber());
            message.setBusinessType(ctx.getBusinessType());
            message.setTxStatus(txStatus.getCode());
            messageProducer.sendMessage(message);
        } catch (Exception e){
            // 更新重试信息
            log.warn("Process 'handleMessage' failed, TXContext=" + ctx, e);
            TransactionInfo info = new TransactionInfo();
            info.setTxId(ctx.getTxId());
            info.setParentId(ctx.getParentId());
            updateRetryCount(lockInfo);
            return;
        }
        //发消息成功后更改TX的状态
        TransactionInfo updInfo = new TransactionInfo();
        updInfo.setParentId(ctx.getParentId());
        updInfo.setTxId(ctx.getTxId());
        updInfo.setTxStatus(TransactionStatusEnum.getNextStatus(txStatus).getCode());
        transactionRepository.update(updInfo);
        log.info("Update Action TX status="+ TransactionStatusEnum.getMemo(updInfo.getTxStatus()) +", TXContext=" + ctx);
    }

    public boolean isValidMessage(TransactionMessage message) throws Throwable {

        //对需处理的数据加锁
        List<TransactionInfo> infos = lockRecord(message);

        if (infos == null || infos.size() == 0) {
            log.warn("Haven't transaction record, message: " + message);
            return false;
        }

        TransactionInfo updInfo;
        int successCount = 0;
        for (TransactionInfo info : infos) {

            if (info.getParentId() == Constants.TX_ROOT_ID) continue;

            if (message.getTxStatus() == TransactionStatusEnum.CONFIRMING.getCode()) {
                if (info.getTxType() != TransactionTypeEnum.TCC.getCode()) {
                    log.warn("Not 'TCC' type transaction, can't perform confirm action, message: " + message);
                    return false;
                }
                if (info.getTxStatus() == TransactionStatusEnum.CONFIRMING.getCode()) {
                    log.warn("In confirming operation, ignored message: " + message);
                    return false;
                }
                if (info.getTxStatus() == TransactionStatusEnum.CONFIRMED.getCode()) {
                    log.warn("Completed confirm operation, ignored message: " + message);
                    return false;
                }
            } else {
                if (info.getTxStatus() == TransactionStatusEnum.CANCELLING.getCode()) {
                    log.warn("In cancelling operation, ignored message: " + message);
                    return false;
                }
                if (info.getTxStatus() == TransactionStatusEnum.CANCELLED.getCode()) {
                    log.warn("Completed cancel operation, ignored message: " + message);
                    return false;
                }
            }

            updInfo = new TransactionInfo();
            updInfo.setParentId(info.getParentId());
            updInfo.setTxId(info.getTxId());
            updInfo.setTxStatus(message.getTxStatus());
            transactionRepository.update(updInfo);
            successCount ++;
            log.info("Valid message and saved to db: " + message + ", status=" + TransactionStatusEnum.getMemo(message.getTxStatus()));
        }

        return successCount > 0;
    }

    // TODO 对于因子事务单元超时引起的事务状态不一致情况，由管控平台统一检查处理?
    /*@Transactional
    public boolean isValidMessage(TransactionMessage message) throws Throwable {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setParentId(message.getParentId());

        if (message.getTxStatus() == TransactionStatusEnum.CANCELLING.getCode()) {
            transactionInfo.setTxStatus(TransactionStatusEnum.CANCELLING.getCode());
            return validAndSaveMessage(transactionInfo, message);
        } else if (message.getTxStatus() == TransactionStatusEnum.CONFIRMING.getCode()) {
            transactionInfo.setTxStatus(TransactionStatusEnum.CONFIRMING.getCode());
            return validAndSaveMessage(transactionInfo, message);
        } else {
            log.warn("Incorrect status, message:" + message);
            return false;
        }
    }

    private boolean validAndSaveMessage(TransactionInfo transactionInfo, TransactionMessage message) throws Throwable {
        if (transactionRepository.find(transactionInfo).size() > 0) {
            log.info("Has main transaction record, ignored message: " + message + ", status=" + TransactionStatusEnum.getMemo(message.getTxStatus()));
            return false;
        } else {
            try {
                transactionRepository.create(transactionInfo);
            } catch (SQLException e) {
                if (e.getSQLState().equals(Constants.KEY_23505)) {
                    log.info("Has main transaction record, ignored message: " + message + ", status=" + TransactionStatusEnum.getMemo(message.getTxStatus()));
                    return false;
                } else {
                    throw e;
                }
            }
            log.info("Valid message and saved to db: " + message + ", status=" + TransactionStatusEnum.getMemo(message.getTxStatus()));
            return true;
        }
    }*/

    public void asyncHandleMessage(final TransactionMessage message) {
        txMsgTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            TransactionMessageService service = SpringContextUtil.getBean(springContextUtil.getModuleId(), TransactionMessageService.class);
            try {
                service.handleMessage(message);
            } catch (Throwable e) {
                log.error("Execute Cancel or Confirm error",e);
            }
            }
        });
    }

    @Transactional
    // TODO 如果一个TX中同一个module参与多次，出现多条事务数据，此处事务需拆分处理
    public void handleMessage(TransactionMessage message) throws Throwable {
        try {
            //从消息中获取事务的标识和业务序列号
            TXContextSupport ctx = new TXContextSupport(message.getParentId(), message.getTxId(), message.getBusinessId(), message.getBusinessType());
            //设置到上下文中
            TXContextHolder.setTXContext(ctx);
            //对需处理的数据加锁
            List<TransactionInfo> infos = lockRecord(message);

            if (infos == null || infos.size() == 0) return;
            for (TransactionInfo info : infos) {
                ctx.setTxId(info.getTxId());
                try {
                    if (info.getParentId() == Constants.TX_ROOT_ID) {
                        // 主事务不参与回滚确认操作
                        log.debug("Root transaction record, ignored in this version.");
                        continue;
                    }
                    if (validation(message, info)) {
                        ServiceExecutePayload payload = parsePayload(info);
                        //根据模块的ApplicationContext获取Bean对象
                        Object aopBean= SpringContextUtil.getBean(info.getModuleId(), payload.getTargetClass());

                        String methodName = null;
                        if (TransactionStatusEnum.CANCELLING.getCode() == message.getTxStatus()) {
                            // 补偿
                            methodName = payload.getCancelMethod();
                            if (StringUtils.isEmpty(methodName)) {
                                log.error("Miss Cancel method, serviceExecutePayload: " + payload);
                                return;
                            }
                        } else if (TransactionStatusEnum.CONFIRMING.getCode() == message.getTxStatus()) {
                            // 确认
                            methodName = payload.getConfirmMethod();
                            if (StringUtils.isEmpty(methodName)) {
                                log.error("Miss Confirm method, serviceExecutePayload: " + payload);
                                return;
                            }
                        }
                        // 执行消息对应的操作
                        ProxyUtil.proxyMethod(aopBean,payload.getTargetClass(),methodName, payload.getTypes(), payload.getArgs());
                    } else {
                        log.warn("Validation error, " + message + ", " + info);
                    }
                } catch (Exception e){
                    // 更新重试信息
                    log.warn("Process 'handleMessage' failed, " + info, e);
                    updateRetryCount(info);
                }
            }
        } finally {
            TXContextHolder.setTXContext(null);
        }
    }

    private List<TransactionInfo> lockRecord(TransactionMessage message) throws Throwable{
        List<TransactionInfo> infos;

        if (message.getTxId() > -1L) {
            // 定时任务调用
            TransactionInfo lockInfo = new TransactionInfo();
            lockInfo.setParentId(message.getParentId());
            lockInfo.setTxId(message.getTxId());
            try {
                infos = transactionRepository.lock(lockInfo);
            } catch (Exception e) {
                throw new DistributedTransactionException("Lock failed, parentId = " + message.getParentId() + ", txId = " + lockInfo.getTxId(), e);
            }
        } else {
            // 消息调用
            List<String> modules;
            if (springContextUtil.getModuleId().equals(Constants.DEFAULT_MODULE_ID)) {
                // 部署在容器中
                modules = SpringContextUtil.getModules();
            } else {
                // 随组件部署
                modules = new ArrayList<String>(1);
                modules.add(springContextUtil.getModuleId());
            }
            try {
                infos = transactionRepository.lockByModules(message.getParentId(), modules);
            } catch (Exception e) {
                throw new DistributedTransactionException("Lock failed, parentId = " + message.getParentId() + ", modules = " + modules, e);
            }
        }
        return infos;
    }

    private void updateRetryCount(TransactionInfo info){
        TransactionInfo updInfo = new TransactionInfo();
        updInfo.setParentId(info.getParentId());
        updInfo.setTxId(info.getTxId());
        RetryCount retryCount = JSON.parseObject(info.getRetriedCount(), RetryCount.class);
        if (retryCount.getCurrentRetryTimes(retryCount, info.getTxStatus()) == 0) {
            // 重试次试为0，设为手动模式
            updInfo.setTxStatus(TransactionStatusEnum.getManulStatusCode(info.getTxStatus()));
        } else {
            updInfo.setRetriedCount(retryCount.getNextRetryTimes(retryCount, info.getTxStatus()));
            updInfo.setNextRetryTime(getNextRetryTime(retryCount, info));
        }
        transactionRepository.update(updInfo);
        log.info("Update retry count, " + updInfo);
    }

    private ServiceExecutePayload parsePayload(TransactionInfo transactionInfo) {
        String json = transactionInfo.getContext();
        //获取模块的名称
        String moduleId= transactionInfo.getModuleId();
        ClassLoader classLoader= SpringContextUtil.getClassLoader(moduleId);
        ParserConfig config= new ParserConfig();
        //指定类加载器
        config.setDefaultClassLoader(classLoader);
        ServiceExecutePayload payload= JSON.parseObject(json, ServiceExecutePayload.class, config, null, JSON.DEFAULT_PARSER_FEATURE, new Feature[0]);
        final Object[] values= payload.getArgs();
        int index=0 ;
        for(Class<?> each: payload.getActualTypes()){
            Object val= values[index];
            if(val!= null) {
                values[index] = JSON.parseObject(val.toString(), each, config, null, JSON.DEFAULT_PARSER_FEATURE, new Feature[0]);
            }
            index++;
        }
        return payload;
    }

    private boolean validation(TransactionMessage message, TransactionInfo txInfo) {
        if (txInfo == null) {
            return false;
        }

        if (message.getTxStatus() == TransactionStatusEnum.CANCELLING.getCode()
                && txInfo.getTxStatus() == TransactionStatusEnum.CANCELLED.getCode()) {
            return false;
        }

        if (message.getTxStatus() == TransactionStatusEnum.CONFIRMING.getCode()
                && txInfo.getTxStatus() == TransactionStatusEnum.CONFIRMED.getCode()) {
            return false;
        }

        return true;
    }

    private Date getNextRetryTime(RetryCount retryCount, TransactionInfo txInfo){
        // TODO 重试次数间隔
        return new Date(System.currentTimeMillis()
                + Math.round(Math.pow(9,
                        retryCount.getDefaultRetryTimes(retryCount, txInfo.getTxStatus())
                                - retryCount.getCurrentRetryTimes(retryCount, txInfo.getTxStatus())))
                * 1000);
    }

}

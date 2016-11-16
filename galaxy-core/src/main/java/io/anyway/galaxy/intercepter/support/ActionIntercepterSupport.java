package io.anyway.galaxy.intercepter.support;

import com.alibaba.fastjson.JSON;
import io.anyway.galaxy.common.Constants;
import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.support.ActionExecutePayload;
import io.anyway.galaxy.context.support.TXContextSupport;
import io.anyway.galaxy.domain.RetryCount;
import io.anyway.galaxy.domain.TransactionInfo;
import io.anyway.galaxy.intercepter.ActionIntercepter;
import io.anyway.galaxy.message.TransactionMessageService;
import io.anyway.galaxy.repository.TransactionIdGenerator;
import io.anyway.galaxy.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * Created by yangzz on 16/7/21.
 */
@Component
@Slf4j
public class ActionIntercepterSupport implements ActionIntercepter{

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMessageService transactionMessageService;

    @Value("${tx.default.msg.retry.times}")
    private int defaultMsgRetryTimes;

    @Value("${tx.default.cancel.retry.times}")
    private int defaultCancelRetryTimes;

    @Value("${tx.default.confirm.retry.times}")
    private int defaultConfirmRetryTimes;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TXContext addAction(String serialNumber,ActionExecutePayload bean)throws Throwable{
        // 新增事务Begin状态
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setTxId(TransactionIdGenerator.next());
        transactionInfo.setParentId(Constants.TX_ROOT_ID); // 事务入口
        transactionInfo.setContext(JSON.toJSONString(bean)); // 当前调用上下文环境
        transactionInfo.setBusinessId(serialNumber); // 业务流水号
        transactionInfo.setBusinessType(bean.getBizType()); // 业务类型
        transactionInfo.setModuleId(bean.getModuleId());
        transactionInfo.setTxType(bean.getTxType().getCode()); // TC | TCC
        transactionInfo.setTxStatus(TransactionStatusEnum.BEGIN.getCode()); //begin状态
        transactionInfo.setRetriedCount(JSON.toJSONString(  // 设置重试次数
                new RetryCount(defaultMsgRetryTimes, defaultCancelRetryTimes, defaultConfirmRetryTimes)));
        createTransactionInfo(transactionInfo);
        // 设置事务上下文
        TXContextSupport ctx= new TXContextSupport();
        ctx.setParentId(transactionInfo.getParentId());
        ctx.setTxId(transactionInfo.getTxId());
        ctx.setTxType(transactionInfo.getTxType());
        ctx.setBusinessType(transactionInfo.getBusinessType());
        ctx.setSerialNumber(serialNumber);
        return ctx;
    }

    @Override
    public void tryAction(TXContext ctx) throws Throwable {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setParentId(ctx.getParentId());
        transactionInfo.setTxId(ctx.getTxId());
        transactionInfo.setTxStatus(TransactionStatusEnum.TRIED.getCode());
        transactionRepository.update(transactionInfo);
    }

    @Override
    public void confirmAction(TXContext ctx) throws Throwable {
        try {
            transactionMessageService.sendMessage(ctx, TransactionStatusEnum.CONFIRMING);
        } catch (Throwable t) {
            log.warn("Send confirm message failed, waiting job retry. TXContext=" + ctx, t);
        }
    }

    @Override
    public void cancelAction(TXContext ctx) throws Throwable {
        try {
            transactionMessageService.sendMessage(ctx, TransactionStatusEnum.CANCELLING);
        } catch (Throwable t) {
            log.warn("Send cancel message failed, waiting job retry. TXContext=" + ctx, t);
        }
    }

    private void createTransactionInfo(TransactionInfo transactionInfo) throws Throwable{
        int i = 2;
        while(i > 0) {
            try {
                transactionRepository.create(transactionInfo);
                break;
            } catch (SQLException e) {
                if (e.getSQLState().equals(Constants.KEY_23505)) {
                    log.warn("Create root transactionInfo record failed and retry:", e);
                    transactionInfo.setTxId(TransactionIdGenerator.next());
                    transactionRepository.create(transactionInfo);
                } else {
                    throw e;
                }
            }
            i--;
        }
    }
}

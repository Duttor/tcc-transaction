package io.anyway.galaxy.recovery;

import io.anyway.galaxy.common.Constants;
import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.common.TransactionTypeEnum;
import io.anyway.galaxy.context.support.TXContextSupport;
import io.anyway.galaxy.domain.TransactionInfo;
import io.anyway.galaxy.message.TransactionMessage;
import io.anyway.galaxy.message.TransactionMessageService;
import io.anyway.galaxy.repository.TransactionRepository;
import io.anyway.galaxy.spring.SpringContextUtil;
import io.anyway.galaxy.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

/**
 * Created by xiong.j on 2016/7/25.
 */
@Slf4j
@Service
public class TransactionRecoveryServiceImpl implements TransactionRecoveryService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMessageService transactionMessageService;

    @Autowired
    private SpringContextUtil springContextUtil;

    /*@Value("${recovery.retry.interval}")
    private long interval = 10 * 1000;*/

    @Value("${recovery.start.day}")
    private int day = 7;

    @Override
    public List<TransactionInfo> fetchData(List<Integer> shardingItem) {
        // 30天前
        Date searchDate = DateUtil.getPrevDate(day);

        // parentId 升序，txId升序
        return transactionRepository.findSince(searchDate, shardingItem.toArray(new Integer[shardingItem.size()]),
                springContextUtil.getModuleId());
    }

    public int execute(List<TransactionInfo> transactionInfos) {
        int successCount = 0;

        long parentId = -1L;
        for(TransactionInfo info : transactionInfos) {

            // 未到重试时间不重试
            /*RetryCount retryCount = JSON.parseObject(info.getRetriedCount(), RetryCount.class);
            int currentRetryTimes = retryCount.getCurrentRetryTimes(retryCount, info.getTxStatus());
            if (info.getGmtModified().getTime() + currentRetryTimes * interval  > System.currentTimeMillis()) {
                continue;
            }*/
            if (info.getParentId() == Constants.TX_ROOT_ID) {
                if(TransactionStatusEnum.BEGIN.getCode() == info.getTxStatus()) {
                    // TODO BEGIN状态需要回查是否Try成功，后续优化
                    try {
                        transactionMessageService.sendMessage(
                                new TXContextSupport(info.getParentId(), info.getTxId(), info.getBusinessId(), info.getBusinessType())
                                , TransactionStatusEnum.CANCELLING);
                        successCount++;
                        log.debug("Transaction recovery job send cancel message success, " + info);
                    } catch (Throwable e) {
                        log.warn("Transaction recovery job send cancel message error, " + info, e);
                    }
                } else if (TransactionStatusEnum.TRIED.getCode() == info.getTxStatus() && TransactionTypeEnum.TCC.getCode() == info.getTxType()){
                    try {
                        transactionMessageService.sendMessage(
                                new TXContextSupport(info.getParentId(), info.getTxId(), info.getBusinessId(), info.getBusinessType())
                                , TransactionStatusEnum.CONFIRMING);
                        successCount++;
                        log.debug("Transaction recovery job send confirm message success, " + info);
                    } catch (Throwable e) {
                        log.warn("Transaction recovery job send confirm message error, " + info, e);
                    }
                } else {
                    log.debug("Transaction recovery job needn't process this record, " + info);
                }
            } else {
                // TODO 对于因子事务单元超时引起的事务状态不一致情况，由管控平台统一检查处理?
                /*if (parentId != info.getParentId() && info.getTxId() == Constants.TX_MAIN_ID) {
                    // 更新每个事务单元的主事务状态
                    TransactionInfo updInfo = new TransactionInfo();
                    updInfo.setParentId(info.getParentId());
                    updInfo.setTxId(Constants.TX_MAIN_ID);
                    try {
                        updInfo.setTxStatus(TransactionStatusEnum.getNextStatus(
                                TransactionStatusEnum.getEnum(info.getTxStatus())
                            ).getCode());
                        transactionRepository.update(updInfo);
                        log.debug("Update main transaction status success, " + info);
                    } catch (Throwable e) {
                        log.warn("Update main transaction status error, " + info.toString());
                    }
                    parentId = info.getParentId();
                    continue;
                }*/

                if (TransactionStatusEnum.CANCELLING.getCode() == info.getTxStatus()) {
                    try {
                        transactionMessageService.handleMessage(transInfo2Msg(info));
                        successCount++;
                        log.debug("Transaction recovery job process cancelling message success, " + info);
                    } catch (Throwable e) {
                        log.warn("Transaction recovery job process cancelling error, " + info, e);
                    }
                }

                if (TransactionStatusEnum.CONFIRMING.getCode() == info.getTxStatus()) {
                    try {
                        transactionMessageService.handleMessage(transInfo2Msg(info));
                        successCount++;
                        log.debug("Transaction recovery job confirming message success, " + info);
                    } catch (Throwable e) {
                        log.warn("Transaction recovery job process confirm error, " + info, e);
                    }
                }
            }
        }
        log.info("Transaction recovery job executed, data count=" + transactionInfos.size() + ", success count=" + successCount);
        return successCount;
    }

    private TransactionMessage transInfo2Msg(TransactionInfo txInfo){
        TransactionMessage txMsg = new TransactionMessage();
        txMsg.setParentId(txInfo.getParentId());
        txMsg.setTxId(txInfo.getTxId());
        txMsg.setBusinessId(txInfo.getBusinessId());
        txMsg.setTxStatus(txInfo.getTxStatus());
        return txMsg;
    }


}

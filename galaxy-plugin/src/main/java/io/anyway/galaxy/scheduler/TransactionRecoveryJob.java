package io.anyway.galaxy.scheduler;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractBatchThroughputDataFlowElasticJob;
import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.domain.TransactionInfo;
import io.anyway.galaxy.recovery.TransactionRecoveryService;
import io.anyway.galaxy.spring.SpringContextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiong.j on 2016/7/29.
 */
public class TransactionRecoveryJob extends AbstractBatchThroughputDataFlowElasticJob<TransactionInfo> {

    private static Map<Integer, Integer> statusMap = initStatus();

    private SpringContextUtil springContextUtil;

    private TransactionRecoveryService transactionRecoveryService;

    @Override
    public List<TransactionInfo> fetchData(JobExecutionMultipleShardingContext shardingContext) {
        if (this.transactionRecoveryService == null) {
            this.transactionRecoveryService = SpringContextUtil.getBean(springContextUtil.getModuleId(), TransactionRecoveryService.class);
        }

        List<Integer> shardingItems = new ArrayList<Integer>(shardingContext.getShardingItems().size());
        // 每个分片对应一个状态
        for (int sharding : shardingContext.getShardingItems()) {
            if (sharding >= statusMap.size()) {
                break;
            }
            shardingItems.add(statusMap.get(sharding));
        }
        return transactionRecoveryService.fetchData(shardingItems);
    }

    @Override
    public boolean isStreamingProcess() {
        return true;
    }

    @Override
    public int processData(JobExecutionMultipleShardingContext shardingContext, List<TransactionInfo> data) {
        return transactionRecoveryService.execute(data);
    }

    private static Map<Integer, Integer> initStatus() {
        statusMap = new HashMap<Integer, Integer>();
        statusMap.put(0, TransactionStatusEnum.BEGIN.getCode());
        statusMap.put(1, TransactionStatusEnum.TRIED.getCode());
        statusMap.put(2, TransactionStatusEnum.CANCELLING.getCode());
        statusMap.put(3, TransactionStatusEnum.CONFIRMING.getCode());
        return statusMap;
    }

    public void setSpringContextUtil(SpringContextUtil springContextUtil) {
        this.springContextUtil = springContextUtil;
    }

}

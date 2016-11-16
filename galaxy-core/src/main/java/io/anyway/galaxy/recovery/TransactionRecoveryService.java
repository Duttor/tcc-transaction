package io.anyway.galaxy.recovery;

import io.anyway.galaxy.domain.TransactionInfo;

import java.util.List;

/**
 * 分布式事务恢复服务
 *
 * Created by xiong.j on 2016/7/21.
 */
public interface TransactionRecoveryService {

    /**
     * 获取待恢复事务
     *
     * @param shardingItem 分片(一个分片对应一个状态)
     * @return
     */
    List<TransactionInfo> fetchData(List<Integer> shardingItem);

    /**
     * 处理待恢复事务
     *
     * @param infos
     * @return
     */
    public int execute(List<TransactionInfo> infos);
}

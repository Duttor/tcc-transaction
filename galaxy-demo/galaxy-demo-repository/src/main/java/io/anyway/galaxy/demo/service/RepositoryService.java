package io.anyway.galaxy.demo.service;

import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.demo.domain.RepositoryDO;

/**
 * Created by yangzz on 16/7/19.
 */
public interface RepositoryService {
    /**
     * 减库存操作
     * @param ctx
     * @param productId 产品ID
     * @param amount 购买产品数量
     * @return
     */
    boolean decreaseRepository(TXContext ctx,long productId, long amount);

    boolean increaseRepository(TXContext ctx,long productId,long amount);
}

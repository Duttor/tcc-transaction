package io.anyway.galaxy.demo.dao;

import io.anyway.galaxy.demo.domain.RepositoryDO;

/**
 * Created by yangzz on 16/7/19.
 */
public interface RepositoryDao {

    /**
     * 新增一种类型产品库存
     * @param repositoryDO
     * @return
     */
    int add(RepositoryDO repositoryDO);

    /**
     * 减库存
     * @param repositoryDO
     * @return
     */
    int decrease(RepositoryDO repositoryDO);

    /**
     * 回滚减库存
     * @param repositoryDO
     * @return
     */
    int increase(RepositoryDO repositoryDO);

}

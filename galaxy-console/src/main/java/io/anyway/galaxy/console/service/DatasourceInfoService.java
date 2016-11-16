package io.anyway.galaxy.console.service;

import io.anyway.galaxy.console.domain.DataSourceInfo;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/8.
 */
public interface DatasourceInfoService {

    /**
     * 增加分布式事务数据源
     * @param dto
     * @return
     */
    int add(DataSourceInfo dto);

    /**
     * 修改分布式事务业务数据源
     * @param dto
     * @return
     */
    int update(DataSourceInfo dto);

    /**
     * 获取分布式事务业务数据源列表
     * @param dto
     * @return
     */
    List<DataSourceInfo> list(DataSourceInfo dto);

    /**
     * 获取分布式事务数据源
     * @param id
     * @return
     */
    DataSourceInfo get(long id);

    /**
     * 删除分布式事务业务类型
     * @param id
     * @return
     */
    int del(long id);
}

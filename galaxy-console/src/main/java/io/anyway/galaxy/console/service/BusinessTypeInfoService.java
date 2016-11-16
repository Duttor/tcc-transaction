package io.anyway.galaxy.console.service;

import io.anyway.galaxy.console.dal.dto.BusinessTypeDto;
import io.anyway.galaxy.console.domain.BusinessTypeInfo;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/4.
 */
public interface BusinessTypeInfoService {

    /**
     * 增加分布式事务业务类型
     * @param dto
     * @return
     */
    int add(BusinessTypeInfo dto);

    /**
     * 修改分布式事务业务类型
     * @param dto
     * @return
     */
    int update(BusinessTypeInfo dto);

    /**
     * 获取分布式事务业务类型列表
     * @param dto
     * @return
     */
    List<BusinessTypeInfo> list(BusinessTypeInfo dto);

    /**
     * 获取分布式事务业务类型与数据源
     * @param id
     * @return
     */
    BusinessTypeInfo get(long id);

    /**
     * 删除分布式事务业务类型
     * @param id
     * @return
     */
    int del(long id);
}

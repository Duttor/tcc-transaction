package io.anyway.galaxy.console.dal.dao;

import io.anyway.galaxy.console.dal.dto.BusinessTypeDto;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/1.
 */
public interface BusinessTypeDao {

    /**
     * 增加分布式事务业务类型
     * @param dto
     * @return
     */
    int add(BusinessTypeDto dto);

    /**
     * 修改分布式事务业务类型
     * @param dto
     * @return
     */
    int update(BusinessTypeDto dto);

    /**
     * 获取分布式事务业务类型列表
     * @param dto
     * @return
     */
    List<BusinessTypeDto> list(BusinessTypeDto dto);

    /**
     * 获取分布式事务业务类型与数据源
     * @param id
     * @return
     */
    BusinessTypeDto get(long id);


    /**
     * 获取分布式事务业务类型与数据源
     * @param name
     * @return
     */
    BusinessTypeDto getByName(String name);

    /**
     * 删除分布式事务业务类型
     * @param id
     * @return
     */
    int del(long id);
}

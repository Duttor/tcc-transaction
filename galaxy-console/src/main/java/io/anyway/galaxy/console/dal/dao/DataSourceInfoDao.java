package io.anyway.galaxy.console.dal.dao;

import io.anyway.galaxy.console.dal.dto.BusinessTypeDto;
import io.anyway.galaxy.console.dal.dto.DataSourceInfoDto;

import java.util.List;


/**
 * Created by xiong.j on 2016/8/1.
 */
public interface DataSourceInfoDao {

    /**
     * 增加数据源
     * @param dto
     * @return
     */
    int add(DataSourceInfoDto dto);

    /**
     * 修改数据源
     * @param dto
     * @return
     */
    int update(DataSourceInfoDto dto);

    /**
     * 获取数据源列表
     * @param dto
     * @return
     */
    List<DataSourceInfoDto> list(DataSourceInfoDto dto);

    /**
     * 获取数据源
     * @param id
     * @return
     */
    DataSourceInfoDto get(long id);

    /**
     * 删除数据源
     * @param id
     * @return
     */
    int del(long id);
}

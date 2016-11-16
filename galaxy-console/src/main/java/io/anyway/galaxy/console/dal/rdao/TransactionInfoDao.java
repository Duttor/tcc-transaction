package io.anyway.galaxy.console.dal.rdao;

import io.anyway.galaxy.console.dal.dto.TransactionInfoDto;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/1.
 */
public interface TransactionInfoDao {

    /**
     * 获取数据源列表 TODO 分页
     * @param dto
     * @return
     */
    List<TransactionInfoDto> list(TransactionInfoDto dto);

    /**
     * 获取数据源
     * @param txId
     * @return
     */
    TransactionInfoDto get(long txId);
}

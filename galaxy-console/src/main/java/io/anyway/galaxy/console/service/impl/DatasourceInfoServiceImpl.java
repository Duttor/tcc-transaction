package io.anyway.galaxy.console.service.impl;

import com.google.common.base.Strings;
import io.anyway.galaxy.console.dal.dao.DataSourceInfoDao;
import io.anyway.galaxy.console.dal.dto.DataSourceInfoDto;
import io.anyway.galaxy.console.domain.DataSourceInfo;
import io.anyway.galaxy.console.service.DatasourceInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/8.
 */
@Service
public class DatasourceInfoServiceImpl implements DatasourceInfoService {

    @Autowired
    private DataSourceInfoDao dataSourceInfoDao;

    @Override
    public int add(DataSourceInfo info) {
        if(!validate(info)) return 0;
        DataSourceInfoDto dto = new DataSourceInfoDto();
        BeanUtils.copyProperties(info, dto);

        return dataSourceInfoDao.add(dto);
    }

    @Override
    public int update(DataSourceInfo info) {
        if(!validate(info)) return 0;

        DataSourceInfoDto dto = new DataSourceInfoDto();
        BeanUtils.copyProperties(info, dto);

        return dataSourceInfoDao.update(dto);
    }

    @Override
    public List<DataSourceInfo> list(DataSourceInfo info) {
        DataSourceInfoDto dto = new DataSourceInfoDto();
        BeanUtils.copyProperties(info, dto);
        List<DataSourceInfoDto> dtos = dataSourceInfoDao.list(dto);

        List<DataSourceInfo> infos = null;
        DataSourceInfo dataSourceInfoDto;

        for(DataSourceInfoDto businessTypeDto : dtos){
            dataSourceInfoDto = new DataSourceInfo();
            BeanUtils.copyProperties(businessTypeDto, dataSourceInfoDto);
            infos.add(dataSourceInfoDto);
        }
        return infos;
    }

    @Override
    public DataSourceInfo get(long id) {
        DataSourceInfoDto dataSourceInfoDto = dataSourceInfoDao.get(id);
        DataSourceInfo dataSourceInfo = new DataSourceInfo();
        BeanUtils.copyProperties(dataSourceInfoDto, dataSourceInfo);
        return dataSourceInfo;
    }

    @Override
    public int del(long id) {
        return dataSourceInfoDao.del(id);
    }

    private boolean validate(DataSourceInfo info){
        if (info.getStatus() == 1 && Strings.isNullOrEmpty(info.getUrl())) return false;
        if (info.getStatus()== 0) {
            if (Strings.isNullOrEmpty(info.getDriverClass()) || Strings.isNullOrEmpty(info.getDbUrl())
                    || Strings.isNullOrEmpty(info.getUsername())) {
                return false;
            }
            if (Strings.isNullOrEmpty(info.getPassword()) || Strings.isNullOrEmpty(info.getJndi())) {
                return false;
            }
        }
        return true;
    }

}

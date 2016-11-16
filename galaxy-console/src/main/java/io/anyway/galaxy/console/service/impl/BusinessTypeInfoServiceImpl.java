package io.anyway.galaxy.console.service.impl;

import io.anyway.galaxy.console.dal.dao.BusinessTypeDao;
import io.anyway.galaxy.console.dal.dto.BusinessTypeDto;
import io.anyway.galaxy.console.domain.BusinessTypeInfo;
import io.anyway.galaxy.console.service.BusinessTypeInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/4.
 */
@Service
public class BusinessTypeInfoServiceImpl implements BusinessTypeInfoService {

    @Autowired
    BusinessTypeDao businessTypeDao;

    @Override
    public int add(BusinessTypeInfo info) {
        BusinessTypeDto dto = new BusinessTypeDto();
        BeanUtils.copyProperties(info, dto);

        return businessTypeDao.add(dto);
    }

    @Override
    public int update(BusinessTypeInfo info) {
        BusinessTypeDto dto = new BusinessTypeDto();
        BeanUtils.copyProperties(info, dto);

        return businessTypeDao.update(dto);
    }

    @Override
    public List<BusinessTypeInfo> list(BusinessTypeInfo info) {
        BusinessTypeDto dto = new BusinessTypeDto();
        BeanUtils.copyProperties(info, dto);
        List<BusinessTypeDto> dtos = businessTypeDao.list(dto);

        List<BusinessTypeInfo> infos = null;
        BusinessTypeInfo businessTypeInfo; 

        for(BusinessTypeDto businessTypeDto : dtos){
            businessTypeInfo = new BusinessTypeInfo();
            BeanUtils.copyProperties(businessTypeDto, businessTypeInfo);
            infos.add(businessTypeInfo);
        }
        return infos;
    }

    @Override
    public BusinessTypeInfo get(long id) {
        BusinessTypeDto businessTypeDto = businessTypeDao.get(id);
        BusinessTypeInfo businessTypeInfo = new BusinessTypeInfo();
        BeanUtils.copyProperties(businessTypeDto, businessTypeInfo);
        return businessTypeInfo;
    }

    @Override
    public int del(long id) {
        return businessTypeDao.del(id);
    }
}

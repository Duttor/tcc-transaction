package io.anyway.galaxy.console.dal.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by xiong.j on 2016/8/1.
 */
@Getter
@Setter
public class BusinessTypeDto {
    private long id;
    private String name;
    private String dsId;
    private int activeFlag;
    private String memo;
    private Timestamp gmtCreated;
    private Timestamp gmtModified;
    private List<DataSourceInfoDto> dataSourceInfoDtos;
}

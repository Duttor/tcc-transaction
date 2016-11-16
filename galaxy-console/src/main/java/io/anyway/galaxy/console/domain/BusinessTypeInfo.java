package io.anyway.galaxy.console.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by xiong.j on 2016/8/1.
 */
@Getter
@Setter
public class BusinessTypeInfo {
    private long id;
    private String name;
    private String dsId;
    private int activeFlag;
    private String memo;
    private Timestamp gmtCreate;
    private Timestamp gmtModified;
    private List<DataSourceInfo> dataSourceInfos;
}

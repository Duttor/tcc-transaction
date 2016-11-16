package io.anyway.galaxy.console.dal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by xiong.j on 2016/8/1.
 */
@Getter
@Setter
@ToString
public class DataSourceInfoDto {
    private long id;
    private String name;
    private String driverClass;
    private String jndi;
    private String dbUrl;
    private String username;
    private String password;
    private int maxActive;
    private int initialSize;
    private String url;
    private int status;
    private int activeFlag;
    private String memo;
    private Timestamp gmtCreated;
    private Timestamp gmtModified;

    // search param
    private List<Long> ids;
}

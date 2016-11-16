package io.anyway.galaxy.console.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * Created by xiong.j on 2016/8/1.
 */
@Getter
@Setter
@ToString
public class DataSourceInfo {
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
    private Timestamp gmtCreate;
    private Timestamp gmtModified;
}

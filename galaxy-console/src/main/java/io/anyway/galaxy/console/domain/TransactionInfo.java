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
public class TransactionInfo {

    private long txId = -1L;

    private long parentId = -1L;

    private String moduleId;

    private String businessId;

    private long businessTypeId = -1L;

    private String businessType;

    private int txStatus = -1;

    private int txType = -1;

    private String context;

    private String payload;

    private int retriedCount = -1;

    private Timestamp gmtCreated;

    private Timestamp gmtModified;
}

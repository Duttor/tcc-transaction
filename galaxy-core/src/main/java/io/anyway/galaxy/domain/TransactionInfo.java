package io.anyway.galaxy.domain;

import java.sql.Date;
import java.text.SimpleDateFormat;

import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.common.TransactionTypeEnum;

/**
 * Created by xiongjie on 2016/7/21.
 */
public class TransactionInfo {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private long txId = -1L;

    private long parentId = -1L;

    private String businessId;

    private String businessType;

    private String moduleId;

    private int txStatus = -1;

    private int txType = -1;

    private String context;

    private Date nextRetryTime;

    private String retriedCount;

    private Date gmtCreated;

    private Date gmtModified;

    public long getTxId() {
        return txId;
    }

    public void setTxId(long txId) {
        this.txId = txId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public int getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(int txStatus) {
        this.txStatus = txStatus;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Date getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(Date nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public String getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(String retriedCount) {
        this.retriedCount = retriedCount;
    }

    public Date getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getStrGmtCreated(){
    	return sdf.format(gmtCreated);
    }
    
    public String getStrGmtModified(){
    	return sdf.format(gmtModified);
    }
    
    public String getStrTXType(){
    	return TransactionTypeEnum.getMemo(txType);
    }
    
    public String getStrTXStatus(){
    	return TransactionStatusEnum.getMemo(txStatus);
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public String toString() {
        return "TransactionInfo{" +
                "txId=" + txId +
                ", parentId=" + parentId +
                ", businessId='" + businessId + '\'' +
                ", businessType='" + businessType + '\'' +
                ", moduleId='" + moduleId + '\'' +
                ", txStatus=" + txStatus +
                ", txType=" + txType +
                ", context='" + context + '\'' +
                ", nextRetryTime=" + nextRetryTime +
                ", retriedCount='" + retriedCount + '\'' +
                ", gmtCreated=" + gmtCreated +
                ", gmtModified=" + gmtModified +
                '}';
    }
}

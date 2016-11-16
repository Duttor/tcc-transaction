package io.anyway.galaxy.message;

import java.io.Serializable;
import java.sql.Date;

/**
 * Created by xiong.j on 2016/7/25.
 */

public class TransactionMessage implements Serializable{

    private long txId = -1L;

    private long parentId = -1L;

    private String businessId;

    private String businessType;

    private int txStatus = -1;

    private Date date= new Date(new java.util.Date().getTime());

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TransactionMessage{" +
                "txId=" + txId +
                ", parentId=" + parentId +
                ", businessId='" + businessId + '\'' +
                ", businessType='" + businessType + '\'' +
                ", txStatus=" + txStatus +
                ", date=" + date +
                '}';
    }
}

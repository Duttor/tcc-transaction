package io.anyway.galaxy.domain;

import com.alibaba.fastjson.JSON;
import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.exception.DistributedTransactionException;
import lombok.*;

/**
 * Created by xiong.j on 2016/8/11.
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class RetryCount {

    // default tried retry count
    private int dMsg;

    // current tried retry count
    private int msg;

    // default cancel retry count
    private int dCancel;

    // current cancel retry count
    private int cancel;

    // default confirm retry count
    private int dConfirm;

    // current cancel retry count
    private int confirm;

    public RetryCount(int dMsg, int dCancel, int dConfirm) {
        this.dMsg = dMsg;
        this.dCancel = dCancel;
        this.dConfirm = dConfirm;
        this.msg = dMsg;
        this.cancel = dCancel;
        this.confirm = dConfirm;
    }

    /**
     * 根据事务状态获取初始设定的重试次数
     * @param retryCount
     * @param txStatus
     * @return
     */
    public int getDefaultRetryTimes(RetryCount retryCount, int txStatus) {

        if (txStatus == TransactionStatusEnum.BEGIN.getCode()) {
            return retryCount.getDMsg();
        } else if (txStatus == TransactionStatusEnum.CANCELLING.getCode()) {
            return retryCount.getDCancel();
        } else if (txStatus == TransactionStatusEnum.CONFIRMING.getCode()) {
            return retryCount.getDConfirm();
        } else {
            throw new DistributedTransactionException("Not support status=" + txStatus + " in here");
        }
    }

    /**
     * 根据事务状态获取当前的重试次数
     * @param retryCount
     * @param txStatus
     * @return
     */
    public int getCurrentRetryTimes(RetryCount retryCount, int txStatus) {

        if (txStatus == TransactionStatusEnum.BEGIN.getCode()) {
            return retryCount.getMsg();
        } else if (txStatus == TransactionStatusEnum.CANCELLING.getCode()) {
            return retryCount.getCancel();
        } else if (txStatus == TransactionStatusEnum.CONFIRMING.getCode()) {
            return retryCount.getConfirm();
        } else {
            throw new DistributedTransactionException("Not support status=" + txStatus + " in here");
        }
    }

    /**
     * 根据事务状态获取下一次的重试次数
     * @param retryCount
     * @param txStatus
     * @return
     */
    public String getNextRetryTimes(RetryCount retryCount, int txStatus) {

        if (txStatus == TransactionStatusEnum.BEGIN.getCode()) {
            retryCount.setMsg(retryCount.getMsg() - 1);
        } else if (txStatus == TransactionStatusEnum.CANCELLING.getCode()) {
            retryCount.setCancel(retryCount.getCancel() - 1);
        } else if (txStatus == TransactionStatusEnum.CONFIRMING.getCode()) {
            retryCount.setConfirm(retryCount.getConfirm() - 1);
        } else {
            throw new DistributedTransactionException("Not support status=" + txStatus + " in here");
        }
        return JSON.toJSONString(retryCount);
    }

    public static void main(String args[]){
        RetryCount r = new RetryCount(5, 5, 3);
        System.out.println(JSON.toJSONString(r));
    }
}

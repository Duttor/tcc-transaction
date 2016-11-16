package io.anyway.galaxy.context.support;

import io.anyway.galaxy.common.TransactionTypeEnum;
import io.anyway.galaxy.context.AbstractExecutePayload;

/**
 * Created by yangzz on 16/7/21.
 */
public class ActionExecutePayload extends AbstractExecutePayload {

    private String actionMethod;

    private int timeout;

    private TransactionTypeEnum txType;

    public ActionExecutePayload(){}

    public ActionExecutePayload(String bizType,String moduleId,Class<?> target, String actionMethod, Class[] types) {
        super(bizType,moduleId,target,types);
        this.actionMethod= actionMethod;
    }

    public String getActionMethod(){
        return actionMethod;
    }

    public void setActionMethod(String actionMethod){
        this.actionMethod= actionMethod;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public TransactionTypeEnum getTxType() {
        return txType;
    }

    public void setTxType(TransactionTypeEnum txType) {
        this.txType = txType;
    }

    @Override
    public String toString(){
        StringBuilder builder= new StringBuilder();
        builder.append("{bizType=")
                .append(getBizType())
                .append(",moduleId=")
                .append(moduleId)
                .append(",timeout=")
                .append(timeout)
                .append(",TxType=")
                .append(getTxType())
                .append(",class=")
                .append(getTargetClass().getName())
                .append(",actionMethod=")
                .append(actionMethod)
                .append(",inTypes=")
                .append(getTypes())
                .append(",inArgs=")
                .append(getArgs())
                .append("}");
        return builder.toString();
    }

    @Override
    public ActionExecutePayload clone(){
        ActionExecutePayload newPayload= new ActionExecutePayload();
        newPayload.bizType= bizType;
        newPayload.moduleId= moduleId;
        newPayload.targetClass= targetClass;
        newPayload.types= types;
        newPayload.txType = txType;
        newPayload.actionMethod= actionMethod;
        return newPayload;
    }
}

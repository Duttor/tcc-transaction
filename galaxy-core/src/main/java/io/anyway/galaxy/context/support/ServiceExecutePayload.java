package io.anyway.galaxy.context.support;

import io.anyway.galaxy.context.AbstractExecutePayload;
import org.springframework.util.StringUtils;

/**
 * Created by yangzz on 16/7/21.
 */
public class ServiceExecutePayload extends AbstractExecutePayload {

    private String tryMethod;

    private String confirmMethod;

    private String cancelMethod;

    public ServiceExecutePayload(){
    }

    public ServiceExecutePayload(String bizType,String moduleId,Class<?> target, String tryMethod, Class[] types) {
        super(bizType,moduleId,target, types);
        this.tryMethod= tryMethod;
    }

    public String getTryMethod(){
        return tryMethod;
    }

    public void setTryMethod(String tryMethod){
        this.tryMethod= tryMethod;
    }

    public String getConfirmMethod() {
        return confirmMethod;
    }

    public void setConfirmMethod(String confirmMethod) {
        this.confirmMethod = confirmMethod;
    }

    public String getCancelMethod() {
        return cancelMethod;
    }

    public void setCancelMethod(String cancelMethod) {
        this.cancelMethod = cancelMethod;
    }

    @Override
    public String toString(){
        StringBuilder builder= new StringBuilder();
        builder.append("{bizType=")
                .append(getBizType())
                .append(",moduleId=")
                .append(moduleId)
                .append(",class=")
                .append(getTargetClass().getName())
                .append(",tryMethod=")
                .append(tryMethod);
        if(!StringUtils.isEmpty(confirmMethod)){
            builder.append(",confirmMethod=")
                    .append(confirmMethod);
        }
        if(!StringUtils.isEmpty(cancelMethod)){
            builder.append(",cancelMethod=")
                    .append(cancelMethod);
        }
        builder.append(",inTypes=")
                .append(getTypes())
                .append(",inArgs=")
                .append(getArgs())
                .append("}");
        return builder.toString();
    }

    @Override
    public ServiceExecutePayload clone(){
        ServiceExecutePayload newPayload= new ServiceExecutePayload();
        newPayload.bizType= bizType;
        newPayload.moduleId= moduleId;
        newPayload.targetClass= targetClass;
        newPayload.types= types;
        newPayload.tryMethod= tryMethod;
        newPayload.confirmMethod= confirmMethod;
        newPayload.cancelMethod= cancelMethod;
        return newPayload;
    }
}

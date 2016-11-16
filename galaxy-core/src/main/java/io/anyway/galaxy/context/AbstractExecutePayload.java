package io.anyway.galaxy.context;

/**
 * Created by yangzz on 16/7/21.
 */
public abstract class AbstractExecutePayload implements Cloneable{

    protected String bizType;

    protected Class<?> targetClass;

    protected Class<?>[] types;

    protected Class<?>[] actualTypes;

    protected Object[] args;

    protected String moduleId;

    public AbstractExecutePayload(){}

    public AbstractExecutePayload(String bizType,String moduleId, Class<?> targetClass, Class<?>[] types){
        this.bizType= bizType;
        this.moduleId= moduleId;
        this.targetClass= targetClass;
        this.types= types;
    }

    public String getBizType(){
        return bizType;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Class<?>[] getTypes(){
        return types;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public void setTypes(Class<?>[] types) {
        this.types = types;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public void setModuleId(String moduleId){
        this.moduleId= moduleId;
    }

    public String getModuleId(){
        return moduleId;
    }

    public Class<?>[] getActualTypes() {
        return actualTypes;
    }

    public void setActualTypes(Class<?>[] actualTypes) {
        this.actualTypes = actualTypes;
    }
}

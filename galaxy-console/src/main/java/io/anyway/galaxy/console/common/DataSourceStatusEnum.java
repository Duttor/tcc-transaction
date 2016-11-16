package io.anyway.galaxy.console.common;

/**
 * Created by xiongjie on 2016/7/21.
 */

public enum DataSourceStatusEnum {

    DB(0, "数据源"),

    HTTP(1, "HTTP"),

    DUBBO(2, "DUBBO");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private DataSourceStatusEnum(int code, String memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {
        return code;
    }

    public String getMemo() {
        return memo;
    }
    
    public static String getMemo(int code) {
    	for(DataSourceStatusEnum type: DataSourceStatusEnum.values()){
    		if(type.code == code){
    			return type.memo;
    		}
    	}
    	return "unknow";
    }
}

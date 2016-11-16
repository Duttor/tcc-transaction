package io.anyway.galaxy.common;

/**
 * Created by xiongjie on 2016/7/21.
 */

public enum TransactionTypeEnum {

    UNKNOWN(-1, "unknown"),

    TC(0, "TC型事务"),

    TCC(1, "TCC型事务"),

    TWOPC(2, "2PC型事务");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private TransactionTypeEnum(int code, String memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {
        return code;
    }

    public String getMemo() {
        return memo;
    }
    
    public static String getMemo(int code){
    	
    	for(TransactionTypeEnum type:TransactionTypeEnum.values()){
    		if(type.code == code){
    			return type.memo;
    		}
    	}
    	return UNKNOWN.getMemo();
    }
}

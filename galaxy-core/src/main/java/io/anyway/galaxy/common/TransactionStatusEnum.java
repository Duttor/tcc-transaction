package io.anyway.galaxy.common;

/**
 * Created by xiongjie on 2016/7/21.
 */

public enum TransactionStatusEnum {
    //SUCCESS(0, "正常完成"),

    UNKNOWN(-1, "unknown"),

    BEGIN(1, "事务开始"),

    TRIED(2, "尝试完成"),

    CANCELLING(3, "回滚中"),

    CANCELLED(4, "回滚完成"),

    CONFIRMING(5, "确认中"),

    CONFIRMED(6, "确认完成"),

    // 达到重试次数，需手动回滚
    MANUAL_CANCEL_WAIT(7, "等待手动回滚"),

    // 达到重试次数，需手动确认
    MANUAL_CONFIRM_WAIT(8, "等待手动确认");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private TransactionStatusEnum(int code, String memo) {
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
    	for(TransactionStatusEnum type:TransactionStatusEnum.values()){
    		if(type.code == code){
    			return type.memo;
    		}
    	}
    	return UNKNOWN.getMemo();
    }

    public static TransactionStatusEnum getNextStatus(TransactionStatusEnum txStatus){
        switch(txStatus){
            case CANCELLING:
                return CANCELLED;
            case CONFIRMING:
                return CONFIRMED;
            default:
                return UNKNOWN;
        }
    }

    public static int getNextStatusCode(TransactionStatusEnum txStatus){
        switch(txStatus){
            case CANCELLING:
                return CANCELLED.getCode();
            case CONFIRMING:
                return CONFIRMED.getCode();
            default:
                return UNKNOWN.getCode();
        }
    }

    public static int getNextStatusCode(int txStatus){
        if (CANCELLING.getCode() == txStatus) {
            return CANCELLED.getCode();
        } else if (CONFIRMING.getCode() == txStatus) {
            return CONFIRMED.getCode();
        } else {
            return UNKNOWN.getCode();
        }
    }

    public static int getManulStatusCode(int txStatus) {
        if (CANCELLING.getCode() == txStatus) {
            return MANUAL_CANCEL_WAIT.getCode();
        } else if (CONFIRMING.getCode() == txStatus) {
            return MANUAL_CONFIRM_WAIT.getCode();
        } else {
            return UNKNOWN.getCode();
        }
    }

    public static TransactionStatusEnum getEnum(int code) {
        for (TransactionStatusEnum item : values()) {
            //不区分大小写
            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

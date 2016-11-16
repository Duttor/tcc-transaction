package io.anyway.galaxy.exception;

/**
 * Created by xiong.j on 2016/7/21
 */
public class DistributedTransactionException extends RuntimeException {

    public DistributedTransactionException(String message) {
        super(message);
    }

    public DistributedTransactionException(Throwable e) {
        super(e);
    }

    public DistributedTransactionException(String message,Throwable e){
        super(message,e);
    }
}

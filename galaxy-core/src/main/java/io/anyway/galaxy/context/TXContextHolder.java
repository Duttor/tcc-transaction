package io.anyway.galaxy.context;

/**
 * Created by yangzz on 16/7/21.
 */
public abstract class TXContextHolder {

    private final static ThreadLocal<TXContext> ctxHolder= new ThreadLocal<TXContext>();

    public static TXContext getTXContext(){
        return ctxHolder.get();
    }

    public static void setTXContext(TXContext ctx){
        ctxHolder.set(ctx);
    }

}

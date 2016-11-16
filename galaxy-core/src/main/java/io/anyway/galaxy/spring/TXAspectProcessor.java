package io.anyway.galaxy.spring;

import io.anyway.galaxy.annotation.TXAction;
import io.anyway.galaxy.annotation.TXTry;
import io.anyway.galaxy.common.TransactionTypeEnum;
import io.anyway.galaxy.context.AbstractExecutePayload;
import io.anyway.galaxy.context.SerialNumberGenerator;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.TXContextHolder;
import io.anyway.galaxy.context.support.ActionExecutePayload;
import io.anyway.galaxy.context.support.ServiceExecutePayload;
import io.anyway.galaxy.exception.DistributedTransactionException;
import io.anyway.galaxy.intercepter.ActionIntercepter;
import io.anyway.galaxy.intercepter.ServiceIntercepter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangzz on 16/7/20.
 */
@Component
@Aspect
public class TXAspectProcessor implements Ordered,ResourceLoaderAware,ApplicationContextAware,ApplicationListener {

    private static Logger logger = LoggerFactory.getLogger(TXAspectProcessor.class);

    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;

    @Autowired
    private DataSourceAdaptor dataSourceAdaptor;

    @Autowired
    private ActionIntercepter actionIntercepter;

    @Autowired
    private ServiceIntercepter serviceIntercepter;

    private ConcurrentHashMap<Method, AbstractExecutePayload> cache= new ConcurrentHashMap<Method, AbstractExecutePayload>();

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    //切面注解TXAction
    @Pointcut("@annotation(io.anyway.galaxy.annotation.TXAction)")
    public void pointcutTXAction(){}

    @Around("pointcutTXAction()")
    public Object doTXAction(ProceedingJoinPoint pjp) throws Throwable {
        if (TXContextHolder.getTXContext()!= null){
            return pjp.proceed();
        }
        //需要判断最外层是否开启了写事务
        assertTransactional();

        //获取方法上的注解内容
        final Method actionMethod = ((MethodSignature) pjp.getSignature()).getMethod();

        String serialNumber= "";
        //根据Action第一个入参获(类型必须是SerialNumberGenerator)取交易流水号
        if(pjp.getArgs().length==0){
            logger.warn("No any incoming parameter,you need input first value typeof SerialNumberGenerator, method: "+actionMethod.getDeclaringClass().getName()+"."+actionMethod.getName());
        }
        else{
            Object firstValue= pjp.getArgs()[0];
            if(!(firstValue instanceof SerialNumberGenerator)){
                logger.warn("The first value is not typeof SerialNumberGenerator, method: "+actionMethod.getDeclaringClass().getName()+"."+actionMethod.getName()+"("+pjp.getArgs()+")");
            }
            else{
                serialNumber= ((SerialNumberGenerator)firstValue).getSerialNumber();
                if(StringUtils.isEmpty(serialNumber)){
                    logger.warn("The incoming trade serial number is empty, method: "+actionMethod.getDeclaringClass().getName()+"."+actionMethod.getName()+"("+pjp.getArgs()+")");
                }
            }
        }
        //缓存actionMethod解析注解的内容
        ActionExecutePayload cachedPayload = (ActionExecutePayload)cache.get(actionMethod);
        for(;cachedPayload==null;){
            //得到方法对应的Class,可能是接口定义
            Class<?> target = actionMethod.getDeclaringClass();
            //得到实现类的方法获取注解的内容
            Method targetMethod= pjp.getTarget().getClass().getDeclaredMethod(actionMethod.getName(),actionMethod.getParameterTypes());
            TXAction action = targetMethod.getAnnotation(TXAction.class);
            String bizType = action.bizType();
            if (StringUtils.isEmpty(bizType)) {
                logger.warn("Miss business type, class: " + pjp.getTarget().getClass() + ",actionMethod: " + actionMethod.getDeclaringClass().getName()+"."+actionMethod.getName());
            }
            String moduleId= SpringContextUtil.getModuleIdByTarget(pjp.getTarget());
            String methodName= actionMethod.getName();
            Class[] types= actionMethod.getParameterTypes();
            cachedPayload= new ActionExecutePayload(bizType,moduleId, target, methodName, types);
            cachedPayload.setTimeout(action.timeout());
            //设置分布式事务类型: TC | TCC
            cachedPayload.setTxType(action.value());
            cache.putIfAbsent(actionMethod, cachedPayload);
            cachedPayload = (ActionExecutePayload)cache.get(actionMethod);
        }
        final ActionExecutePayload payload= cachedPayload.clone();
        //设置运行时的入参
        payload.setArgs(pjp.getArgs());
        //设置入参的真实类型
        payload.setActualTypes(toActualTypes(payload.getTypes(),pjp.getArgs()));

        try {
            //获取新的连接开启新事务新增一条TX记录
            final TXContext ctx = actionIntercepter.addAction(serialNumber,payload);
            //更改TX记录状态为TRIED
            if (logger.isInfoEnabled()) {
                Method method= pjp.getTarget().getClass().getDeclaredMethod(actionMethod.getName(),actionMethod.getParameterTypes());
                logger.info("Main transaction start and save 'begin' status to db, TXContext:" + ctx+", actionMethod: "
                        + method.getDeclaringClass().getName()+"."+method.getName()+", actionExecutePayload: "+payload);
            }

            //绑定到ThreadLocal中
            TXContextHolder.setTXContext(ctx);
//            //设置在Action操作里
//            TXContextHolder.setAction(true);

            //获取外层业务开启事务的对应的数据库连接
            ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSourceAdaptor.getDataSource());
            final Connection conn = conHolder.getConnection();
            Method setterMethod= ReflectionUtils.findMethod(ConnectionHolder.class,"setConnection",Connection.class);
            ReflectionUtils.makeAccessible(setterMethod);
            ReflectionUtils.invokeMethod(setterMethod,conHolder,
                Proxy.newProxyInstance(resourceLoader.getClassLoader(),
                        //重载Connection复写commit和rollback方法
                        new Class<?>[]{Connection.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try {
                            return method.invoke(conn,args);
                        }finally {
                            //确保在commit执行之后执行通知方法
                            if ("commit".equals(method.getName())) {
                                //如果是TCC类型事务才发送confirm消息
                                if(payload.getTxType()== TransactionTypeEnum.TCC){
                                    if (logger.isInfoEnabled()) {
                                        logger.info("Will send confirm message, TXContext: " + ctx);
                                    }
                                    actionIntercepter.confirmAction(ctx);
                                }
                                //确保在cancel之后执行通知方法
                            } else if ("rollback".equals(method.getName())) {
                                if (logger.isInfoEnabled()) {
                                    logger.info("Will send cancel message, TXContext: " + ctx);
                                }
                                actionIntercepter.cancelAction(ctx);
                            }
                        }
                    }
                }));
            //执行业务操作
            Object result= pjp.proceed();
            actionIntercepter.tryAction(ctx);
            if (logger.isInfoEnabled()) {
                Method method= pjp.getTarget().getClass().getDeclaredMethod(actionMethod.getName(),actionMethod.getParameterTypes());
                logger.info("Main transaction 'try' succeed and save 'tried' status to db, TXContext:" + ctx+", actionMethod: "
                        + method.getDeclaringClass().getName()+"."+method.getName());
            }
            return result;

        }finally{
            //清空上下文内容
            TXContextHolder.setTXContext(null);
        }
    }

    //切面注解TXTry
    @Pointcut("@annotation(io.anyway.galaxy.annotation.TXTry)")
    public void pointcutTXTry(){ }

    @Around("pointcutTXTry()")
    public Object doTXTry(ProceedingJoinPoint pjp) throws Throwable {
        TXContext ctx= TXContextHolder.getTXContext();
        if(ctx ==null && pjp.getArgs().length> 0){
            //规定第一个参数为TXContext里面传递txId等信息
            Object firstValue= pjp.getArgs()[0];
            if(firstValue instanceof TXContext){
                ctx= (TXContext) firstValue;
            }
        }
        if(ctx== null){
            if(logger.isInfoEnabled()){
                logger.info("TXContext is empty ,try="+pjp.getSignature());
            }
            return pjp.proceed();
        }
        //验证事务是否为空
        assertTransactional();

        //获取方法上的注解内容
        final Method tryMethod = ((MethodSignature) pjp.getSignature()).getMethod();
        //缓存serviceMethod解析注解的内容
        ServiceExecutePayload cachedPayload = (ServiceExecutePayload)cache.get(tryMethod);
        for(;cachedPayload==null;){
            //得到方法对应的Class,可能是接口定义
            Class<?> target = tryMethod.getDeclaringClass();
            //得到实现类的方法获取注解的内容
            Method targetMethod= pjp.getTarget().getClass().getDeclaredMethod(tryMethod.getName(),tryMethod.getParameterTypes());
            TXTry txTry= targetMethod.getAnnotation(TXTry.class);
            String bizType = txTry.bizType();
            if (StringUtils.isEmpty(bizType)) {
                logger.warn("Miss business type, class: " + pjp.getTarget().getClass() + ",tryMethod: " + targetMethod.getDeclaringClass().getName()+"."+targetMethod.getName());
            }
            String moduleId= SpringContextUtil.getModuleIdByTarget(pjp.getTarget());
            String methodName= tryMethod.getName();
            Class[] types= tryMethod.getParameterTypes();
            cachedPayload= new ServiceExecutePayload(bizType,moduleId, target, methodName, types);
            cachedPayload.setConfirmMethod(txTry.confirm());
            cachedPayload.setCancelMethod(txTry.cancel());
            cache.putIfAbsent(tryMethod, cachedPayload);
            cachedPayload = (ServiceExecutePayload)cache.get(tryMethod);
        }
        ServiceExecutePayload payload= cachedPayload.clone();
        //设置运行时的入参
        payload.setArgs(pjp.getArgs());
        //设置入参的真实类型
        payload.setActualTypes(toActualTypes(payload.getTypes(),pjp.getArgs()));

        //先调用业务方法
        Object result= pjp.proceed();
        //更改TX状态为TRIED`
        serviceIntercepter.tryService(ctx,payload);
        if (logger.isInfoEnabled()) {
            Method method= pjp.getTarget().getClass().getDeclaredMethod(tryMethod.getName(),tryMethod.getParameterTypes());
            logger.info("Child transaction 'try' succeed and save 'tried' status to db, TXContext:" + ctx+", tryMethod: "
                    + method.getDeclaringClass().getName()+"."+method.getName()+", serviceExecutePayload: "+payload);
        }
        return result;
    }

    //切面注解TXConfirm
    @Pointcut("@annotation(io.anyway.galaxy.annotation.TXConfirm)")
    public void pointcutTXConfirm(){}

    @Around("pointcutTXConfirm()")
    public Object doTXConfirm(ProceedingJoinPoint pjp) throws Throwable {

        TXContext ctx= TXContextHolder.getTXContext();
        if(ctx ==null && pjp.getArgs().length> 0){
            //规定第一个参数为TXContext里面传递txId等信息
            Object firstValue= pjp.getArgs()[0];
            if(firstValue instanceof TXContext){
                ctx= (TXContext) firstValue;
            }
        }
        if(ctx== null){
            if(logger.isInfoEnabled()){
                logger.info("TXContext is empty ,confirm="+pjp.getSignature());
            }
            return pjp.proceed();
        }
        //验证事务是否为空
        assertTransactional();

        Object result= pjp.proceed();
        //更改TX表的状态为CONFIRMED
        serviceIntercepter.confirmService(ctx);

        if (logger.isInfoEnabled()) {
            Method method= ((MethodSignature) pjp.getSignature()).getMethod();
            method= pjp.getTarget().getClass().getDeclaredMethod(method.getName(),method.getParameterTypes());
            logger.info("Child transaction 'confirm' succeed and save 'cancelled' status to db, TXContext:" + ctx+", confirmMethod: "
                    + method.getDeclaringClass().getName()+"."+method.getName());
        }
        return result;
    }

    //切面注解TXCancel
    @Pointcut("@annotation(io.anyway.galaxy.annotation.TXCancel)")
    public void pointcutTXCancel(){}

    @Around("pointcutTXCancel()")
    public Object doTXCancel(ProceedingJoinPoint pjp) throws Throwable {

        TXContext ctx= TXContextHolder.getTXContext();
        if(ctx ==null && pjp.getArgs().length> 0){
            //规定第一个参数为TXContext里面传递txId等信息
            Object firstValue= pjp.getArgs()[0];
            if(firstValue instanceof TXContext){
                ctx= (TXContext) firstValue;
            }
        }
        if(ctx== null){
            if(logger.isInfoEnabled()){
                logger.info("TXContext is empty ,cancel="+pjp.getSignature());
            }
            return pjp.proceed();
        }
        //验证事务是否为空
        assertTransactional();

        Object result= pjp.proceed();
        //更改TX表的状态为CANCELLED
        serviceIntercepter.cancelService(ctx);

        if (logger.isInfoEnabled()) {
            Method method= ((MethodSignature) pjp.getSignature()).getMethod();
            method= pjp.getTarget().getClass().getDeclaredMethod(method.getName(),method.getParameterTypes());
            logger.info("Child transaction 'cancel' succeed and save 'cancelled' status to db, TXContext:" + ctx+", cancelMethod: "
                    + method.getDeclaringClass().getName()+"."+method.getName());
        }
        return result;
    }

    private void assertTransactional()throws Throwable{
        DataSource dataSource= dataSourceAdaptor.getDataSource();
        Assert.notNull(dataSource,"datasource can not empty");
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (conHolder == null || conHolder.getConnectionHandle()==null || !conHolder.isSynchronizedWithTransaction()) {
            throw new DistributedTransactionException("transaction connection is null");
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader= resourceLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext= applicationContext;
    }

    private Class<?>[] toActualTypes(Class<?>[] types,Object[] inArgs){
        Class<?>[] actualTypes= new Class<?>[types.length];
        for(int i=0;i<types.length;i++){
            actualTypes[i]= inArgs[i]==null? types[i]: inArgs[i].getClass();
        }
        return actualTypes;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

    }
}


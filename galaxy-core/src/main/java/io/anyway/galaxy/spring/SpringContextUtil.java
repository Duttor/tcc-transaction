package io.anyway.galaxy.spring;

import io.anyway.galaxy.context.ModuleContext;
import io.anyway.galaxy.context.ModuleContextAdapter;
import io.anyway.galaxy.exception.DistributedTransactionException;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiong.j on 2016/7/25.
 */
public class SpringContextUtil implements ApplicationContextAware,ResourceLoaderAware,InitializingBean,DisposableBean,ModuleContext {

    private ClassLoader classLoader;

    private ApplicationContext applicationContext;

    private String moduleId= "";

    private static ConcurrentHashMap<String,ModuleContext> moduleContexts= new ConcurrentHashMap<String,ModuleContext>();

    /**
     * 获取对象
     * @param  moduleId
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    public static Object getBean(String moduleId,String name) throws BeansException {
        return getApplicationContext(moduleId).getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     * 如果bean不能被类型转换，相应的异常将会被抛出（BeanNotOfRequiredTypeException）
     * @param  moduleId
     * @param name       bean注册名
     * @param requiredType 返回对象类型
     * @return Object 返回requiredType类型对象
     * @throws BeansException
     */
    public static <T> T getBean(String moduleId,String name, Class<T> requiredType) throws BeansException {
        return getApplicationContext(moduleId).getBean(name, requiredType);
    }

    /**
     * 获取类型为requiredType的对象
     * 如果bean不能被类型转换，相应的异常将会被抛出（BeanNotOfRequiredTypeException）
     * @param  moduleId
     * @param requiredType 返回对象类型
     * @return Object 返回requiredType类型对象
     * @throws BeansException
     */
    public static <T> T getBean(String moduleId,Class<T> requiredType) throws BeansException {
        ApplicationContext ctx= getApplicationContext(moduleId);
        return ctx.getBean(requiredType);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     * @param  moduleId
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String moduleId,String name) {
        return getApplicationContext(moduleId).containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
     * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     * @param  moduleId
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     */
    public static boolean isSingleton(String moduleId,String name) throws NoSuchBeanDefinitionException {
        return getApplicationContext(moduleId).isSingleton(name);
    }

    /**
     * 获取类型
     * @param  moduleId
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     */
    public static Class getType(String moduleId,String name) throws NoSuchBeanDefinitionException {
        return getApplicationContext(moduleId).getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     * @param  moduleId
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String moduleId,String name) throws NoSuchBeanDefinitionException {
        return getApplicationContext(moduleId).getAliases(name);
    }

    /**
     * 获取模块的应用上下文
     * @param moduleId
     * @return
     */
    public static ApplicationContext getApplicationContext(String moduleId){
        return moduleContexts.get(moduleId).getApplicationContext();
    }

    /**
     * 获取模块的类加载器
     * @param moduleId
     * @return
     */
    public static ClassLoader getClassLoader(String moduleId){
        return moduleContexts.get(moduleId).getClassLoader();
    }

    /**
     * 根据模块中的实例获取模块标识
     * @param target
     * @return
     */
    public static String getModuleIdByTarget(Object target){
        if(target instanceof ModuleContextAdapter){
            return ((ModuleContextAdapter)target).getModuleContext().getModuleId();
        }
        //如果是Aop代理则需要获取targetClass的ClassLoader
        ClassLoader classLoader= target.getClass().getClassLoader();
        if(target instanceof Advised){
            classLoader= ((Advised)target).getTargetClass().getClassLoader();
        }
        for(ModuleContext each: moduleContexts.values()){
            if(each.getClassLoader()==classLoader){
                return each.getModuleId();
            }
        }
        return "webapplication";
    }

    /**
     * 获取容器中管理的所有模块标识
     * @return
     */
    public static List<String> getModules(){
        List<String> modules = new ArrayList<String>();
        for (String key : moduleContexts.keySet()) {
            modules.add(key);
        }
        return modules;
    }

    public void setModuleId(String moduleId){
        this.moduleId= moduleId;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.classLoader= resourceLoader.getClassLoader();
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String getModuleId() {
        return moduleId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(StringUtils.isEmpty(moduleId)){
            moduleId= applicationContext.getApplicationName();
        }
        if(moduleContexts.contains(moduleId)){
            throw new DistributedTransactionException("duplicated applicationContext register, moduleId: "+moduleId);
        }
        moduleContexts.putIfAbsent(moduleId,this);
    }

    @Override
    public void destroy() throws Exception {
        moduleContexts.remove(moduleId);
    }
}
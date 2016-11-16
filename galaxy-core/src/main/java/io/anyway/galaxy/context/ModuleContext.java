package io.anyway.galaxy.context;

import org.springframework.context.ApplicationContext;

/**
 * Created by yangzz on 16/8/3.
 */
public interface ModuleContext {

    /**
     * 获取模块的Spring应用上线文
     * @return
     */
    ApplicationContext getApplicationContext();

    /**
     * 获取模块的类加载器
     * @return
     */
    ClassLoader getClassLoader();

    /**
     * 获取模块的唯一标识
     * @return
     */
    String getModuleId();

}

package io.anyway.galaxy.console.dal.db;


import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Strings;
import io.anyway.galaxy.console.dal.dao.DataSourceInfoDao;
import io.anyway.galaxy.console.dal.dto.DataSourceInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiong.j on 2016/8/1.
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource implements ApplicationContextAware {

    private DataSourceInfoDao dataSourceInfoDao;

    private ApplicationContext applicationContext;

    private Map<Long, DataSource> cacheDataSource = new ConcurrentHashMap<Long, DataSource>();

    private static final String dataSourceBeanClass = "org.mybatis.spring.SqlSessionFactoryBean";

    private static final String dataSourceName = "dynamicDataSource";

    private DataSource defaultDataSource;

    public DynamicDataSource(){
        setTargetDataSources(Collections.emptyMap());
    }

    protected DataSource determineTargetDataSource() {

        // 获取线程上下文所需使用的数据源
        DataSourceInfoDto dataSourceInfoDto = DsTypeContextHolder.getDsInfo();

        if (dataSourceInfoDto == null) return defaultDataSource;

        if (cacheDataSource.containsKey(dataSourceInfoDto.getId())) {
            return cacheDataSource.get(dataSourceInfoDto.getId());
        } else {
            // 根据查询的数据源信息生成数据源
            //createDataSource(getDataSourceInfoDto(dataSourceInfoDto));
            createDataSource(dataSourceInfoDto);
        }

        return cacheDataSource.get(dataSourceInfoDto.getId());
    }

    public synchronized void createDataSource(DataSourceInfoDto dto) {

        if (cacheDataSource.containsKey(dto.getId())) return;

        DataSource dataSource = null;
        if (Strings.isNullOrEmpty(dto.getJndi())) {
            dataSource = getDruidDataSource(dto);
        } else {
            dataSource = getJndiDataSource(dto.getJndi());
        }

        cacheDataSource.put(dto.getId(), dataSource);
    }

    private DataSource getDruidDataSource(DataSourceInfoDto dto) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dto.getDbUrl());
        druidDataSource.setUsername(dto.getUsername());
        druidDataSource.setPassword(getPassword(dto.getUsername(), dto.getPassword()));
        druidDataSource.setMaxActive(dto.getMaxActive());
        druidDataSource.setInitialSize(dto.getInitialSize());
        druidDataSource.setMaxWait(60000);
        druidDataSource.setTimeBetweenEvictionRunsMillis(300000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setValidationQuery("select 1");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setPoolPreparedStatements(true);
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(50);
        try {
            druidDataSource.init();
        } catch (SQLException e) {
            log.error("Can't init druidDataSource from DataSourceInfo:" + dto, e);
        }
        return druidDataSource;
    }

    private DataSource getJndiDataSource(String jndi) {
        DataSource jndiDatasource = null;
        /*try {
            Context context = new InitialContext();
            Context envContext = null;
            try {
                envContext = (Context) context.lookup("java:/comp/env");
            } catch (Exception e) {
                // 无法通过java:方式获得换用/comp/env的方式
                try {
                    envContext = (Context) context.lookup("/comp/env");
                } catch (Exception e1) {
                    log.error("Can't get datasource from JNDI:" + jndi, e1);
                }
            }
            //如果数据源的名称不为空的话使用指定的数据源的名称来获取数据库连接对象
            if(!Strings.isNullOrEmpty(jndi)) {
                jndiDatasource = (DataSource) envContext.lookup(jndi);
            }
        } catch (NamingException e) {
            log.error("Can't get datasource from JNDI:" + jndi, e);
        }*/

        jndiDatasource = resolveSpecifiedDataSource(jndi);

        return jndiDatasource;
    }

    private DataSourceInfoDto getDataSourceInfoDto(long id) {
        // 切换线程上下文使用本地库查询数据源信息后，还原线程上下文
        String contextType = DsTypeContextHolder.getContextType();
        DsTypeContextHolder.setContextType(DsTypeContextHolder.DEFAULT_SESSION_FACTORY);

        if (dataSourceInfoDao == null) {
            dataSourceInfoDao = applicationContext.getBean("dataSourceInfoDao", io.anyway.galaxy.console.dal.dao.DataSourceInfoDao.class);
        }
        DataSourceInfoDto dataSourceInfoDto = dataSourceInfoDao.get(id);
        DsTypeContextHolder.setContextType(contextType);

        return dataSourceInfoDto;
    }

    private String getPassword(String username, String password) {
        if (Strings.isNullOrEmpty(password)) {
            // TODO Cyber
        } else {
            return password;
        }
        return "";
    }

    public void close(){
        for (Map.Entry<Long, DataSource> entry : cacheDataSource.entrySet()) {
            if (entry.getValue() instanceof DruidDataSource){
                ((DruidDataSource) entry.getValue()).close();
            }
        }
        cacheDataSource.clear();
    }

    /*private void registerDataSourceBean(DataSourceInfo dto) {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(dataSourceBeanClass);
        beanDefinitionBuilder.getBeanDefinition().setAttribute("id", "dynamicDataSource" + dto.getId());
        beanDefinitionBuilder.addPropertyValue("url", dto.getUrl());
        beanDefinitionBuilder.addPropertyValue("username", dto.getUsername());
        beanDefinitionBuilder.addPropertyValue("password", getPassword(dto.getUsername(), dto.getPassword()));
        beanDefinitionBuilder.addPropertyValue("maxActive", dto.getMaxActive());
        beanDefinitionBuilder.addPropertyValue("initialSize", dto.getInitialSize());
        beanDefinitionBuilder.addPropertyValue("maxWait", 600000);
        beanDefinitionBuilder.addPropertyValue("timeBetweenEvictionRunsMillis", 300000);
        beanDefinitionBuilder.addPropertyValue("minEvictableIdleTimeMillis", 300000);
        beanDefinitionBuilder.addPropertyValue("validationQuery", "select 1");
        beanDefinitionBuilder.addPropertyValue("testWhileIdle", true);
        beanDefinitionBuilder.addPropertyValue("testOnBorrow", false);
        beanDefinitionBuilder.addPropertyValue("testOnReturn", false);
        beanDefinitionBuilder.addPropertyValue("poolPreparedStatements", true);
        beanDefinitionBuilder.addPropertyValue("maxPoolPreparedStatementPerConnectionSize", 50);
        defaultListableBeanFactory.registerBeanDefinition("dynamicDataSource" + dto.getId(), beanDefinitionBuilder.getBeanDefinition());
    }*/

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DsTypeContextHolder.getDsInfo();
    }

    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }
}

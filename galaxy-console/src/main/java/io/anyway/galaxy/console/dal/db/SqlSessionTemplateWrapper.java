package io.anyway.galaxy.console.dal.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * Created by xiong.j on 2016/8/3.
 */
class SqlSessionTemplateWrapper implements FactoryBean<SqlSessionTemplate> ,InitializingBean{

    private SqlSessionFactory sqlSessionFactory;

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    private Map<Object, SqlSessionFactory> targetSqlSessionFactorys;

    public void setTargetSqlSessionFactorys(Map<Object, SqlSessionFactory> targetSqlSessionFactorys) {
        this.targetSqlSessionFactorys = targetSqlSessionFactorys;
    }

    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public SqlSessionTemplate getObject() throws Exception {
        return sqlSessionTemplate;
    }

    @Override
    public Class<?> getObjectType() {
        return SqlSessionTemplate.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(sqlSessionFactory);
        customSqlSessionTemplate.setTargetSqlSessionFactorys(targetSqlSessionFactorys);
        sqlSessionTemplate = customSqlSessionTemplate;
    }
}
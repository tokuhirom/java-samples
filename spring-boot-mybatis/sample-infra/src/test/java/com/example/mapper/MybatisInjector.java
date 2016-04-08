package com.example.mapper;

import org.apache.ibatis.session.SqlSession;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.sql.Connection;

public class MybatisInjector {
    private final SqlSession sqlSession;

    public MybatisInjector(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public void inject(Class<?> klass, Object target) {
        // init
        for (Field field : klass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);

                try {
                    Class<?> type = field.getType();
                    Object bean = getBean(type);
                    field.set(target, bean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public <T> T getBean(Class<T> klass) {
        if (klass.isAssignableFrom(SqlSession.class)) {
            return (T) sqlSession;
        } else if (klass.isAssignableFrom(Connection.class)) {
            return (T) sqlSession.getConnection();
        } else {
            return sqlSession.getMapper(klass);
        }
    }
}

package com.example.mapper;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class DaoTestRunner extends Runner implements Filterable {
    private final BlockJUnit4ClassRunner runner;

    public DaoTestRunner(Class<?> klass) throws InvocationTargetException, InitializationError, IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        runner = new BlockJUnit4ClassRunner(klass) {
            private SqlSession sqlSession;

            protected Statement withBefores(FrameworkMethod method, Object target,
                                            Statement statement) {
                this.sqlSession = sqlSessionFactory.openSession();

                // init
                for (Field field : klass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        field.setAccessible(true);
                        try {
                            field.set(target, sqlSession.getMapper(field.getType()));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return super.withBefores(method, target, statement);
            }

            @Override
            protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
                if (this.sqlSession != null) {
                    this.sqlSession.close();
                    this.sqlSession = null;
                }
                return super.withAfters(method, target, statement);
            }
        };
    }

    @Override
    public Description getDescription() {
        return runner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        runner.run(notifier);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        runner.filter(filter);
    }
}

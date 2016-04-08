package com.example.mapper;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;

public abstract class DaoTestBase {
    private SqlSession sqlSession;

    @Before
    public void before() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(inputStream);

        this.sqlSession = sqlSessionFactory.openSession(false);
        MybatisInjector mybatisInjector = new MybatisInjector(
                sqlSession);
        mybatisInjector.inject(getClass(), this);
    }

    @After
    public void after() {
        sqlSession.close();
    }

    // Note: junit5 provides smarter way to extend test suite.
    // https://github.com/junit-team/junit5/wiki/Prototype-Test-Extensions
}

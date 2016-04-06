package com.example.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * Created by tokuhirom on 4/7/16.
 */
@RunWith(DaoTestRunner.class)
public class BlogMapperTest {
    @Inject
    private BlogMapper blogMapper;

    @Test
    public void findAll() throws Exception {
        blogMapper.findAll();
    }

}
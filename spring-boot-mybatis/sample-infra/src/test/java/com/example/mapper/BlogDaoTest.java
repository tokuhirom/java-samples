package com.example.mapper;

import com.example.domain.Blog;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BlogDaoTest extends DaoTestBase {
    @Inject
    BlogDao blogDao;

    @Test
    public void findAll() throws Exception {
        Blog blog = new Blog();
        blog.setTitle("hoge");

        blogDao.insert(blog);
        assertThat(blog.getId()).isNotEqualTo(0);

        List<Blog> blogs = blogDao.findAll();
        assertThat(blogs).contains(blog);
    }

}

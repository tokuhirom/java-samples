package com.example.mapper;

import com.example.domain.Blog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BlogDao {
    List<Blog> findAll();

    void insert(Blog blog);

    @Select("SELECT * FROM blog WHERE id=#{id}")
    List<Blog> findById(String id);
}

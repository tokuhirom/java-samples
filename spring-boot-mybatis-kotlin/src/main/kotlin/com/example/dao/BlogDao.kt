package com.example.dao

import com.example.entity.Blog
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface BlogDao {
    @Select("""
        SELECT * FROM blog
    """)
    fun findAll(): List<Blog>

    @Select("""
        SELECT * FROM blog WHERE id=#{id}
    """)
    fun findById(@Param("id") id: Long): Blog
}

package com.example.controller;

import com.example.domain.Blog;
import com.example.mapper.BlogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BlogController {
    @Autowired
    private BlogDao blogDao;

    @RequestMapping("/blog/")
    public List<Blog> blogs() {
        return blogDao.findAll();
    }

    @RequestMapping("/blog/{id}")
    public List<Blog> blogs(@PathVariable("id") String id) {
        return blogDao.findById(id);
    }
}

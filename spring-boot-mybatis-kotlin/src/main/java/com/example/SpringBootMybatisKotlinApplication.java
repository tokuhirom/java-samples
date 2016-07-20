package com.example;

import com.example.dao.BlogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootMybatisKotlinApplication implements CommandLineRunner {
    @Autowired
    BlogDao blogDao;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMybatisKotlinApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(this.blogDao.findAll());
    }
}

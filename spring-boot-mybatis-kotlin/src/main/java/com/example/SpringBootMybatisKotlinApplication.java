package com.example;

import com.example.dao.BlogDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SpringBootMybatisKotlinApplication implements CommandLineRunner {
    @Autowired
    BlogDao blogDao;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMybatisKotlinApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println(this.blogDao.findAll());
        } else {
            if ("findById".equals(args[0])) {
                log.info("findById: {}", this.blogDao.findById(Long.valueOf(args[1])));
            } else {
                log.error("Unknown command: {}", args[0]);
            }
        }
    }
}

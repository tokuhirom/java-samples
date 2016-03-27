package com.example.service;

import com.example.aop.annotation.Hello;
import lombok.Data;

@Data
public class HelloService {
    private Long x;

    @Hello
    public String hi() {
        this.setX(3L);
        return "hi";
    }
}

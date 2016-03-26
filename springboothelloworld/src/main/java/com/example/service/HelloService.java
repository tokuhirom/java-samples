package com.example.service;

import com.example.aop.annotation.Hello;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Data
public class HelloService {
    private Long x;

    @Hello
    public String hi() {
        this.setX(3L);
        return "hi";
    }
}

package com.example.service;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Data
public class HelloService {
    private Long x;

    public String hi() {
        this.setX(3L);
        return "hi";
    }
}

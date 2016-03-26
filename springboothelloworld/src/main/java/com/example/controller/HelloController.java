package com.example.controller;

import com.example.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloController {
    @Autowired
    private HelloService helloService;

    @RequestMapping("/")
    public String hello() {
        return "hello";
    }

    @RequestMapping("/hi")
    public String hi() {
        return "hello";
    }

}

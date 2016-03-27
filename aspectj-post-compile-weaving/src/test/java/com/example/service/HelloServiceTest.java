package com.example.service;

import com.example.aop.MyAOP;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloServiceTest {
    @Test
    public void hi() throws Exception {
        MyAOP.beforeCount.set(0);
        HelloService helloService = new HelloService();
        assertEquals(helloService.hi(), "hi");
        assertEquals(1, MyAOP.beforeCount.get());
    }
}
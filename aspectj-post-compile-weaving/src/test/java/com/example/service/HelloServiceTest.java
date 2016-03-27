package com.example.service;

import com.example.aop.MyAOP;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloServiceTest {
    @Test
    public void hi() throws Exception {
        assertEquals(0, MyAOP.beforeCount.get());
        HelloService helloService = new HelloService();
        assertEquals(helloService.hi(), "hi");
        assertEquals(1, MyAOP.beforeCount.get());
    }
}
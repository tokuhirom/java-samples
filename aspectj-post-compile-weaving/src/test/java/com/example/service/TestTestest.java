package com.example.service;

import com.example.aop.MyAOP;
import com.example.aop.annotation.Hello;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTestest {
    @Test
    public void hello() {
        TestTest testTest = new TestTest();
        assertEquals(0, MyAOP.beforeCount.get());
        testTest.hello();
        assertEquals(1, MyAOP.beforeCount.get());
    }
}

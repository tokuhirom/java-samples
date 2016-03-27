package com.example.service;

import com.example.aop.MyAOP;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTestest {
    @Test
    public void hello() {
        MyAOP.beforeCount.set(0);
        TestTest testTest = new TestTest();
        testTest.hello();
        assertEquals(1, MyAOP.beforeCount.get());
    }
}

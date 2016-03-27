package com.example.aop;

import com.example.aop.annotation.Hello;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.concurrent.atomic.AtomicInteger;

@Aspect
public class MyAOP {
    public static AtomicInteger beforeCount = new AtomicInteger(0);

    @Around("execution(* com.example..*.*(..)) && @annotation(hello)")
    public Object foo(ProceedingJoinPoint joinPoint, Hello hello) throws Throwable {
        beforeCount.incrementAndGet();

        System.out.println("BEFORE HELLO!");
        System.out.println(joinPoint.getSignature());
        Object o = joinPoint.proceed();
        System.out.println("AFTER HELLO");
        return o;
    }
}

package com.example.aop;

import com.example.aop.annotation.Hello;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MyAOP {
    @Around("execution(* com.example..*.*(..)) && @annotation(hello)")
    public Object foo(ProceedingJoinPoint joinPoint, Hello hello) throws Throwable {
        System.out.println("BEFORE HELLO!");
        System.out.println(joinPoint.getSignature());
        Object o = joinPoint.proceed();
        System.out.println("AFTER HELLO");
        return o;
    }
}

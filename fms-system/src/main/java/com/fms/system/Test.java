package com.fms.system;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Test {
    @Before("execution(* com.fms.system.service..*.*(..))")   // 在所有方法执行前
    public void beginTx() {
        System.out.println("开始了");
    }

    @AfterReturning("execution(* com.fms.system.service..*.*(..))")
    public void commitTx() {
        System.out.println("提交了");
    }

    @AfterThrowing("execution(* com.fms.system.service..*.*(..))")
    public void rollbackTx() {
        System.out.println("回滚了");
    }
}

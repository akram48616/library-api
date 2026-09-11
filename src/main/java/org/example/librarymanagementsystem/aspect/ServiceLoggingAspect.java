package org.example.librarymanagementsystem.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    // ─── Pointcut: every method in every service ────
    @Pointcut("execution(* org.example.librarymanagementsystem.Service.*.*(..))")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object logServiceDuration(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.debug("Service call {} completed in {} ms", methodName, duration);
            return result;

        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.warn("Service call {} failed after {} ms - {}: {}",
                    methodName, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
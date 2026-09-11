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
public class ApiLoggingAspect {
    // ─── Pointcut: every method in every controller ─
    @Pointcut("execution(* org.example.librarymanagementsystem.Controller.*.*(..))")
    public void controllerMethods() {}
    @Around("controllerMethods()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName=joinPoint.getSignature().toShortString();
        long start=System.currentTimeMillis();
        try{
            Object result=joinPoint.proceed();
            long duration=System.currentTimeMillis()-start;
            log.info("API call {} completed in {} ms",methodName,duration);
            return result;
        }catch(Throwable ex
        ){
            long duration=System.currentTimeMillis()-start;
            log.warn("API call {} failed after {} ms - {}: {}",
                    methodName, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

}

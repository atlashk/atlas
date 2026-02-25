package org.atlas.libs.framework.security.authorization;

import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RequiredRoleAspect {

    @Pointcut("@within(org.atlas.libs.framework.security.authorization.RequiredRole)")
    public void classLevel() {}

    @Pointcut("@annotation(org.atlas.libs.framework.security.authorization.RequiredRole)")
    public void methodLevel() {}

    @Before("execution(public * *(..)) && (classLevel() || methodLevel())")
    public void checkRole(JoinPoint joinPoint) {
        UserRole currentRole = Contexts.getUserRole();
        if (currentRole == null) {
            throw new BaseDomainException(CommonDomainError.UNAUTHORIZED);
        }

        // Prioritize method annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method interfaceMethod = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method method;
        try {
            method = targetClass.getMethod(
                interfaceMethod.getName(),
                interfaceMethod.getParameterTypes()
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        RequiredRole methodAnnotation = method.getAnnotation(RequiredRole.class);
        RequiredRole classAnnotation = targetClass.getAnnotation(RequiredRole.class);
        UserRole requiredRole = null;
        if (methodAnnotation != null) {
            requiredRole = methodAnnotation.value();
        } else if (classAnnotation != null) {
            requiredRole = classAnnotation.value();
        }
        if (requiredRole == null) {
            return;
        }

        if (currentRole != requiredRole) {
            throw new BaseDomainException(CommonDomainError.FORBIDDEN);
        }
    }
}

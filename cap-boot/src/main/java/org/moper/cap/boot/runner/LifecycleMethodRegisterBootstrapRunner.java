package org.moper.cap.boot.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.util.BeanLifecycleResolver;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

/**
 * 扫描已注册的 BeanDefinition，为其补充 @Capper 注解中声明的生命周期方法信息。
 *
 * <p>执行顺序为 320，在 {@link ClassBeanRegisterBootstrapRunner}（300）和
 * {@link FactoryBeanRegisterBootstrapRunner}（310）之后执行。
 *
 * <p>对于普通类 Bean，从 {@link BeanDefinition#type()} 上查找 {@link Capper} 注解；
 * 对于工厂 Bean，{@link BeanDefinition#type()} 即为工厂方法的返回类型，同样从该类型上查找注解。
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 320, description = "Register lifecycle methods (init and destroy) from @Capper annotations")
public class LifecycleMethodRegisterBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();
        String[] beanNames = container.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition beanDef = container.getBeanDefinition(beanName);
            Class<?> beanType = beanDef.type();

            Capper capperAnnotation = beanType.getAnnotation(Capper.class);
            if (capperAnnotation == null) {
                continue;
            }

            String initMethod = capperAnnotation.initMethod();
            String destroyMethod = capperAnnotation.destroyMethod();

            boolean hasInit = !initMethod.isBlank();
            boolean hasDestroy = !destroyMethod.isBlank();

            if (!hasInit && !hasDestroy) {
                continue;
            }

            if (hasInit) {
                BeanLifecycleResolver.validate(beanType, initMethod);
            }
            if (hasDestroy) {
                BeanLifecycleResolver.validate(beanType, destroyMethod);
            }

            BeanDefinition updated = beanDef
                    .withInitMethod(hasInit ? initMethod : null)
                    .withDestroyMethod(hasDestroy ? destroyMethod : null);
            container.removeBeanDefinition(beanName);
            container.registerBeanDefinition(updated);
            log.info("Register lifecycle methods for bean '{}': initMethod={}, destroyMethod={}",
                    beanName, hasInit ? initMethod : "none", hasDestroy ? destroyMethod : "none");
        }
    }
}

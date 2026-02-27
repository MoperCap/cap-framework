package org.moper.cap.boot;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.context.annotation.ComponentScan;
import org.moper.cap.context.context.RuntimeContext;

@Slf4j
@ComponentScan("org.moper.cap")
public class CapApplicationTest {

    @Test
    void test1() throws Exception {
        try(RuntimeContext context = new DefaultCapApplication(CapApplicationTest.class).run()){
            CapApplicationTest clazz = context.getBean(CapApplicationTest.class);
            clazz.print();
        }
    }

    void print(){
        log.info("hello world");
    }
}

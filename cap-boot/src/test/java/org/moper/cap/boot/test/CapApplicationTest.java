package org.moper.cap.boot.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.definition.BeanScope;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.annotation.ComponentScan;
import org.moper.cap.core.annotation.ResourceScan;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.property.officer.PropertyOfficer;

@Slf4j

public class CapApplicationTest {


    @ComponentScan("org.moper.cap.boot.test")
    @ResourceScan("true")
    public static class TrueConfigurationClass{

        @Capper(names = {"true1", "TrueConfigurationClass1"}, primary = false, lazy = true, scope = BeanScope.PROTOTYPE, description = "one true1 configuration class")
        public static FalseConfigurationClass falseConfigurationClass1(){
            return new FalseConfigurationClass(2);
        }

        @Capper(names = {"true2", "TrueConfigurationClass2"}, primary = false, lazy = true, scope = BeanScope.SINGLETON, description = "one true2 configuration class")
        public FalseConfigurationClass falseConfigurationClass2(){
            return new FalseConfigurationClass(2);
        }
    }

    @Capper(names = {"false", "FalseConfigurationClass"}, primary = true, lazy = false, scope = BeanScope.SINGLETON, description = "one false configuration class")
    @ComponentScan("org.moper.cap.boot.test")
    @ResourceScan("false")
    public static class FalseConfigurationClass{
        private int index;

        public FalseConfigurationClass(){
            this.index = 1;
        }

        public FalseConfigurationClass(int index){
            this.index = index;
        }

        public void print(){
            log.info("index={}", index);
        }
    }



    @Test
    void propertyTest() throws Exception {
        try(RuntimeContext context = new DefaultCapApplication(TrueConfigurationClass.class).run()){
            PropertyOfficer officer = context.getPropertyOfficer();

            String studentName = officer.getPropertyValue("student.name", String.class);
            int studentAge = officer.getPropertyValue("student.age", Integer.class);
            String studentSex = officer.getPropertyValue("student.sex", String.class);
            log.info("student[name={}, age={}, sex={}]", studentName, studentAge, studentSex);

            String teacherName = officer.getPropertyValue("teacher.name", String.class);
            int teacherAge = officer.getPropertyValue("teacher.age", Integer.class);
            String teacherSex = officer.getPropertyValue("teacher.sex", String.class);
            log.info("teacher[name={}, age={}, sex={}]", teacherName, teacherAge, teacherSex);
        }
    }

    @Test
    void beanTest() throws Exception {
        try(RuntimeContext context = new DefaultCapApplication(TrueConfigurationClass.class).run()){
            FalseConfigurationClass falseConfigurationBean = context.getBean("false", FalseConfigurationClass.class);
            falseConfigurationBean.print();

            FalseConfigurationClass trueConfigurationBean1 = context.getBean("TrueConfigurationClass1", FalseConfigurationClass.class);
            trueConfigurationBean1.print();

            FalseConfigurationClass trueConfigurationBean2 = context.getBean("TrueConfigurationClass2", FalseConfigurationClass.class);
            trueConfigurationBean2.print();
        }
    }
}

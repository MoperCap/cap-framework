package org.moper.cap.boot.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.annotation.ComponentScan;
import org.moper.cap.core.annotation.ResourceScan;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.property.officer.PropertyOfficer;

@Slf4j

public class CapApplicationTest {


    @ComponentScan("org.moper.cap.boot.test")
    @ResourceScan("true")
    class TrueConfigurationClass{}

    @ComponentScan("org.moper.cap.boot.test")
    @ResourceScan("false")
    class FalseConfigurationClass{}

    @Test
    void test1() throws Exception {
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
}

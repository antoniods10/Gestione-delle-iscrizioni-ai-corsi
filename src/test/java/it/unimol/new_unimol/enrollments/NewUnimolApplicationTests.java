package it.unimol.new_unimol.enrollments;

import it.unimol.new_unimol.enrollments.service.AdminService;
import it.unimol.new_unimol.enrollments.service.StudentService;
import it.unimol.new_unimol.enrollments.service.TeacherService;
import it.unimol.new_unimol.enrollments.service.TokenJWTService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NewUnimolApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private TokenJWTService tokenJWTService;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void testServiciesAreNotNull() {
        assertAll(
                () -> assertNotNull(studentService, "StudentService dovrebbe essere caricato"),
                () -> assertNotNull(teacherService, "TeacherService dovrebbe essere caricato"),
                () -> assertNotNull(adminService, "AdminService dovrebbe essere caricato"),
                () -> assertNotNull(tokenJWTService, "TokenJWTService dovrebbe essere caricato")
        );
    }

    @Test
    void testApplicationContextContainsBeans() {
        assertTrue(applicationContext.containsBean("studentService"));
        assertTrue(applicationContext.containsBean("teacherService"));
        assertTrue(applicationContext.containsBean("adminService"));
    }

}

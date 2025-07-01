package it.unimol.new_unimol.enrollments.stub;

import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationRequestDto;
import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationResponseDto;
import it.unimol.new_unimol.enrollments.stub.event.CourseDeactivatedEvent;
import it.unimol.new_unimol.enrollments.stub.event.CourseDeletedEvent;
import it.unimol.new_unimol.enrollments.stub.event.TeacherChangedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub service per simulare il microservizio Gestione Corsi durante lo sviluppo e i test
 * Attivare con il profilo Spring "stub"
 */
@Service
@Profile("stub")
public class CourseStubService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.microservicies}")
    private String microservicesExchange;

    @Value("${rabbitmq.routing.course.validation.response}")
    private String courseValidationResponseRoutingKey;

    //Database simulato di corsi
    private static final Map<String, CourseData> MOCK_COURSES = new HashMap<>();

    static {
        MOCK_COURSES.put("CORSO001", new CourseData(
            "CORSO001",
                "Microservizi",
                "Corso di programmazione Java con Spring Boot",
                "TEACHER001",
                true,
                100,
                6,
                "Informatica"
        ));

        MOCK_COURSES.put("CORSO002", new CourseData(
                "CORSO002",
                "Basi di dati modulo 2",
                "Corso di introduzione a Blockchain e database NoSQL",
                "TEACHER002",
                true,
                80,
                6,
                "Informatica"
        ));

        MOCK_COURSES.put("CORSO001ì3", new CourseData(
                "CORSO003",
                "Reti di Calcolatori modulo 2",
                "Corso di programmazione su reti",
                "TEACHER001",
                true,
                50,
                6,
                "Informatica"
        ));

        MOCK_COURSES.put("CORSO004", new CourseData(
                "CORSO004",
                "Economia Aziendale",
                "Principi di economia e gestione aziendale",
                "TEACHER003",
                false,
                120,
                6,
                "Economia"
        ));

        MOCK_COURSES.put("CORSO005", new CourseData(
                "CORSO005",
                "Cloud Computing AWS",
                "Corso di architetture cloud con AWS",
                "TEACHER002",
                true,
                60,
                9,
                "Informatica"
        ));

    }

    /**
     * Ascolta le richieste di validazione del corso
     */
    @RabbitListener(queues = "${rabbitmq.queue.course.validation}")
    public void handleCourseValidationRequest(CourseValidationRequestDto request) {
        System.out.println("[COURSE STUB] Ricevuta richiesta di validazione corso: " + request.courseId());

        CourseValidationResponseDto response;

        CourseData course = MOCK_COURSES.get(request.courseId());

        if(course != null) {
            response = new CourseValidationResponseDto(
                    course.courseId,
                    course.courseName,
                    course.description,
                    course.isActive,
                    course.isActive, //isValid
                    course.teacherId,
                    course.maxStudents,
                    course.isActive ? null : "Corso non attivo",
                    request.correlationId()

            );
        } else {
            response = new CourseValidationResponseDto(
                    request.courseId(),
                    null,
                    null,
                    false,
                    false,
                    null,
                    null,
                    "Corso non trovato nel sistema",
                    request.correlationId()
            );
        }

        rabbitTemplate.convertAndSend(
                microservicesExchange,
                courseValidationResponseRoutingKey,
                response,
                message -> {
                    message.getMessageProperties().setCorrelationId(request.correlationId());
                    return message;
                }
        );

        System.out.println("[COURSE STUB] Risposta inviata per corso: " + request.courseId() + " - Valido: " + response.isValid());
    }

    /**
     * Simula la pubblicazione di eventi quando viene modificato
     */
    public void simulateCourseDeleted(String courseId) {
        CourseDeletedEvent event = new CourseDeletedEvent();
        event.setCourseId(courseId);
        event.setDeletedBy("ADMIN");
        event.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                microservicesExchange,
                "course.deleted",
                event
        );

        System.out.println("[COURSE STUB] Pubblicato evento CourseDeleted per: " + courseId);
    }

    /**
     * Simula la pubblicazione di eventi quando un corso viene disattivato
     */
    public void simulateCourseDeactivated(String courseId) {
        CourseDeactivatedEvent event = new CourseDeactivatedEvent();
        event.setCourseId(courseId);
        event.setDeactivatedBy("ADMIN");
        event.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                microservicesExchange,
                "course.deactivated",
                event
        );

        System.out.println("[COURSE STUB] Pubblicato evento CourseDeactivated per: " + courseId);
    }

    /**
     * Simula l'aggiornamento del docente di un corso
     */
    public void simulateTeacherChanged(String courseId, String oldTeacherId, String newTeacherId) {
        TeacherChangedEvent event = new TeacherChangedEvent();
        event.setCourseId(courseId);
        event.setOldTeacherId(oldTeacherId);
        event.setNewTeacherId(newTeacherId);
        event.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                microservicesExchange,
                "course.teacher.changed",
                event
        );

        System.out.println("[COURSE STUB] Pubblicato evento TeacherChanged per corso: " + courseId);
    }

    private static class CourseData {
        final String courseId;
        final String courseName;
        final String description;
        final String teacherId;
        final boolean isActive;
        final int maxStudents;
        final int credits;
        final String department;

        CourseData(String courseId, String courseName, String description, String teacherId,
                   boolean isActive, int maxStudents, int credits, String department) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.description = description;
            this.teacherId = teacherId;
            this.isActive = isActive;
            this.maxStudents = maxStudents;
            this.credits = credits;
            this.department = department;
        }
    }

    /**
     * Metodi per testing
     */
    public static void addMockCourse(String courseId, String courseName, String description,
                                     String teacherId, boolean isActive, int maxStudents) {
        MOCK_COURSES.put(courseId, new CourseData(courseId, courseName, description, teacherId, isActive, maxStudents, 6, "Test"));
    }

    public static void removeMockCourse(String courseId) {
        MOCK_COURSES.remove(courseId);
    }

    public static void clearMockCourses() {
        MOCK_COURSES.clear();
    }

    public static boolean courseExists(String courseId) {
        return MOCK_COURSES.containsKey(courseId);
    }

}

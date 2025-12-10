package it.unimol.new_unimol.enrollments.service.corsi;

import it.unimol.new_unimol.enrollments.dto.corsi.CourseResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CourseServiceClient {
    private final RestTemplate restTemplate;

    @Value("${courses.service.url:http://localhost:8081}")
    private String coursesServiceUrl;

    public CourseServiceClient() {
        this.restTemplate = new RestTemplate();;
    }

    /**
     * Verifica se un corso esiste
     */
    public boolean courseExists(String courseId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    coursesServiceUrl + "/api/courses/" + courseId + "/exists",
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ottiene i dettagli di un corso
     */
    public CourseResponseDto getCourseById(String courseId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CourseResponseDto> response = restTemplate.exchange(
                coursesServiceUrl + "/api/courses/" + courseId,
                HttpMethod.GET,
                entity,
                CourseResponseDto.class
        );

        return response.getBody();
    }

    /**
     * Ottieni tutti i corsi disponibili
     */
    public List<CourseResponseDto> getAllCourses(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<CourseResponseDto>> response = restTemplate.exchange(
                coursesServiceUrl + "/api/courses",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<CourseResponseDto>>() {}
        );

        return response.getBody();
    }
}

package it.unimol.new_unimol.enrollments.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.enrollment}")
    private String enrollmentExchange;

    @Value("${rabbitmq.exchange.microservicies}")
    private String microserviciesExchange;

    @Value("${rabbitmq.queue.user.validation}")
    private String userValidationQueue;

    @Value("${rabbitmq.queue.course.validation}")
    private String courseValidationQueue;

    @Value("${rabbitmq.queue.user.validation.response}")
    private String userValidationResponseQueue;

    @Value("${rabbit.queue.course.validation.response}")
    private String courseValidationResponseQueue;

    @Value("${rabbit.queue.notification}")
    private String notificationQueue;

    @Value("${rabbitmq.routing.user.validation}")
    private String userValidationRoutingKey;

    @Value("${rabbitmq.routing.course.validation}")
    private String courseValidationRoutingKey;

    @Value("${rabbitmq.routing.user.validation.response}")
    private String userValidationResponseRoutingKey;

    @Value("${rabbitmq.routing.course.validation.response}")
    private String courseValidationResponseRoutingKey;

    @Value("${rabbitmq.routing.notification}")
    private String notificationRoutingKey;

    //Exchanges
    @Bean
    public TopicExchange ernollmentExchange(){
        return new TopicExchange(enrollmentExchange);
    }

    @Bean
    public TopicExchange microserviciesExchange(){
        return new TopicExchange(microserviciesExchange);
    }

    //Code per validazione utenti
    @Bean
    public Queue userValidationQueue() {
        return QueueBuilder.durable(userValidationQueue).build();
    }

    @Bean
    public Binding userValidationBinding() {
        return BindingBuilder
                .bind(userValidationQueue())
                .to(microserviciesExchange())
                .with(userValidationRoutingKey);
    }

    //Code per validazione corsi
    @Bean
    public Queue courseValidationQueue() {
        return QueueBuilder.durable(courseValidationQueue).build();
    }

    @Bean
    public Binding courseValidationBinding() {
        return BindingBuilder
                .bind(courseValidationQueue())
                .to(microserviciesExchange())
                .with(courseValidationRoutingKey);
    }

    //Code per risposte validazione utenti
    @Bean
    public Queue userValidationResponseQueue() {
        return QueueBuilder.durable(userValidationResponseQueue).build();
    }

    @Bean
    public Binding userValidationResponseBinding() {
        return BindingBuilder
                .bind(userValidationResponseQueue())
                .to(microserviciesExchange())
                .with(userValidationResponseRoutingKey);
    }

    //Code per risposte validazione corsi
    @Bean
    public Queue courseValidationResponseQueue() {
        return QueueBuilder.durable(courseValidationResponseQueue).build();
    }

    @Bean
    public Binding courseValidationResponseBinding() {
        return BindingBuilder
                .bind(courseValidationResponseQueue())
                .to(microserviciesExchange())
                .with(courseValidationResponseRoutingKey);
    }

    //Code per notifiche
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(microserviciesExchange())
                .with(notificationRoutingKey);
    }

    //Converter per JSON
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    public String getEnrollmentExchange() {
        return this.enrollmentExchange;
    }

    public String getMicroserviciesExchange() {
        return this.microserviciesExchange;
    }

    public String getUserValidationQueue() {
        return this.userValidationQueue;
    }

    public String getCourseValidationQueue() {
        return this.courseValidationQueue;
    }

    public String getUserValidationResponseQueue() {
        return this.userValidationResponseQueue;
    }

    public String getCourseValidationResponseQueue() {
        return this.courseValidationResponseQueue;
    }

    public String getNotificationQueue() {
        return this.notificationQueue;
    }

    public String getUserValidationRoutingKey() {
        return this.userValidationRoutingKey;
    }

    public String getCourseValidationRoutingKey() {
        return this.courseValidationRoutingKey;
    }

    public String getUserValidationResponseRoutingKey() {
        return this.userValidationResponseRoutingKey;
    }

    public String getCourseValidationResponseRoutingKey() {
        return this.courseValidationResponseRoutingKey;
    }

    public String getNotificationRoutingKey() {
        return this.notificationRoutingKey;
    }
}

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

    @Value("${rabbitmq.queue.enrollment.created}")
    private String enrollmentCreatedQueue;

    @Value("${rabbitmq.routing.enrollment.created}")
    private String enrollmentCreatedRoutingKey;

    @Value("${rabbitmq.queue.enrollment.approved}")
    private String enrollmentApprovedQueue;

    @Value("${rabbitmq.routing.enrollment.approved}")
    private String enrollmentApprovedRoutingKey;

    @Value("${rabbitmq.queue.enrollment.rejected}")
    private String enrollmentRejectedQueue;

    @Value("${rabbitmq.routing.enrollment.rejected}")
    private String enrollmentRejectedRoutingKey;

    @Value("${rabbitmq.queue.enrollment.deleted}")
    private String enrollmentDeletedQueue;

    @Value("${rabbitmq.routing.enrollment.deleted}")
    private String enrollmentDeletedRoutingKey;

    @Value("${rabbitmq.queue.enrollment.updated}")
    private String enrollmentUpdatedQueue;

    @Value("${rabbitmq.routing.enrollment.updated}")
    private String enrollmentUpdatedRoutingKey;

    @Value("${rabbitmq.queue.enrollment.request.submitted}")
    private String enrollmentRequestSubmittedQueue;

    @Value("${rabbitmq.routing.enrollment.request.submitted}")
    private String enrollmentRequestSubmittedRoutingKey;

    @Value("${rabbitmq.queue.enrollment.notification}")
    private String enrollmentNotificationQueue;

    @Value("${rabbitmq.routing.enrollment.notification}")
    private String enrollmentNotificationRoutingKey;

    //Exchanges
    @Bean
    public TopicExchange enrollmentExchange(){
        return new TopicExchange(enrollmentExchange);
    }

    @Bean
    public Queue enrollmentCreatedQueue() {
        return QueueBuilder.durable(enrollmentCreatedQueue).build();
    }

    @Bean
    public Binding enrollmentCreatedBinding() {
        return BindingBuilder
                .bind(enrollmentCreatedQueue())
                .to(enrollmentExchange())
                .with(enrollmentCreatedRoutingKey);
    }

    @Bean
    public Queue enrollmentApprovedQueue() {
        return QueueBuilder.durable(enrollmentApprovedQueue).build();
    }

    @Bean
    public Binding enrollmentApprovedBinding() {
        return BindingBuilder
                .bind(enrollmentApprovedQueue())
                .to(enrollmentExchange())
                .with(enrollmentApprovedRoutingKey);
    }

    @Bean
    public Queue enrollmentRejectedQueue() {
        return QueueBuilder.durable(enrollmentRejectedQueue).build();
    }

    @Bean
    public Binding enrollmentRejectedBinding() {
        return BindingBuilder
                .bind(enrollmentRejectedQueue())
                .to(enrollmentExchange())
                .with(enrollmentRejectedRoutingKey);
    }

    @Bean
    public Queue enrollmentDeletedQueue() {
        return QueueBuilder.durable(enrollmentDeletedQueue).build();
    }

    @Bean
    public Binding enrollmentDeletedBinding() {
        return BindingBuilder
                .bind(enrollmentDeletedQueue())
                .to(enrollmentExchange())
                .with(enrollmentDeletedRoutingKey);
    }

    @Bean
    public Queue enrollmentUpdatedQueue() {
        return QueueBuilder.durable(enrollmentUpdatedQueue).build();
    }

    @Bean
    public Binding enrollmentUpdatedBinding() {
        return BindingBuilder
                .bind(enrollmentUpdatedQueue())
                .to(enrollmentExchange())
                .with(enrollmentUpdatedRoutingKey);
    }

    @Bean
    public Queue enrollmentRequestSubmittedQueue() {
        return QueueBuilder.durable(enrollmentRequestSubmittedQueue).build();
    }

    @Bean
    public Binding enrollmentRequestSubmittedBinding() {
        return BindingBuilder
                .bind(enrollmentRequestSubmittedQueue())
                .to(enrollmentExchange())
                .with(enrollmentRequestSubmittedRoutingKey);
    }

    @Bean
    public Queue enrollmentNotificationQueue() {
        return QueueBuilder.durable(enrollmentNotificationQueue).build();
    }

    @Bean
    public Binding enrollmentNotificationBinding() {
        return BindingBuilder
                .bind(enrollmentNotificationQueue())
                .to(enrollmentExchange())
                .with(enrollmentNotificationRoutingKey);
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

    public String getEnrollmentCreatedQueue(){return this.enrollmentCreatedQueue;}

    public String getEnrollmentApprovedQueue(){return this.enrollmentApprovedQueue;}

    public String getEnrollmentRejectedQueue(){return this.enrollmentRejectedQueue;}

    public String getEnrollmentDeletedQueue(){return this.enrollmentDeletedQueue;}

    public String getEnrollmentUpdatedQueue(){return this.enrollmentUpdatedQueue;}

    public String getEnrollmentNotificationQueue(){return this.enrollmentNotificationQueue;}

    public String getEnrollmentRequestSubmittedQueue(){return this.enrollmentRequestSubmittedQueue;}

    public String getEnrollmentCreatedRoutingKey(){return this.enrollmentCreatedRoutingKey;}

    public String getEnrollmentApprovedRoutingKey(){return this.enrollmentApprovedRoutingKey;}

    public String getEnrollmentRejectedRoutingKey() {return this.enrollmentRejectedRoutingKey;}

    public String getEnrollmentDeletedRoutingKey() {return this.enrollmentDeletedRoutingKey;}

    public String getEnrollmentUpdatedRoutingKey() {return this.enrollmentUpdatedRoutingKey;}

    public String getEnrollmentRequestSubmittedRoutingKey() {return this.enrollmentRequestSubmittedRoutingKey;}

    public String getEnrollmentNotificationRoutingKey() {return this.enrollmentNotificationRoutingKey;}
}

package saulo.brustolin.notification.configurations;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue transactionCreatedQueue() {
        return new Queue("notifications.v1.transaction-created", true);
    }
}

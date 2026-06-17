package saulo.brustolin.notification.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Notification {
    
    @RabbitListener(queues = "notifications.v1.transaction-created")
    public void sendCreatedTransactionMail(String message) {
        System.out.println(message);
    }
}

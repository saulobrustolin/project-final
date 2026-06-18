package saulo.brustolin.notification.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Notification {
    
    @RabbitListener(queues = "#{transactionCreatedQueue.name}")
    public void sendCreatedTransactionMail(String message) {
        System.out.println(message);
    }
}

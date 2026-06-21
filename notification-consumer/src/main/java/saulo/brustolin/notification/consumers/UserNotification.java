package saulo.brustolin.notification.consumers;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import lombok.AllArgsConstructor;
import saulo.brustolin.notification.configurations.RabbitMQConfig;
import saulo.brustolin.notification.configurations.ResendConfig;
import saulo.brustolin.shared.dtos.VerificationCodeEvent;

@Component
@AllArgsConstructor
public class UserNotification {

    private final Resend resend;
    private final TemplateEngine templateEngine;
    
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "notifications.v1.user", durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.EXCHANGE_NAME, type = "direct"),
        key = "user.updated"
    ))
    public void sendVerificationCodeMail(VerificationCodeEvent message) {
        Context context = new Context();
        context.setVariables(Map.of(
            "fullname", message.fullName(),
            "email", message.email()
        ));
        String htmlContent = templateEngine.process("user-updated", context);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from(String.format("usuário atualizado. <%s>", ResendConfig.EMAIL_FROM))
            .to(message.email())
            .subject("usuário atualizado.")
            .html(htmlContent)
            .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package saulo.brustolin.notification.consumers;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.resend.services.emails.model.CreateEmailOptions;

import lombok.AllArgsConstructor;
import saulo.brustolin.notification.configurations.RabbitMQConfig;
import saulo.brustolin.notification.configurations.ResendConfig;
import saulo.brustolin.shared.dtos.VerificationCodeEvent;

@Component
@AllArgsConstructor
public class VerificationCodeNotification {

    private final ResendConfig resendConfig;
    private final TemplateEngine templateEngine;
    
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "notifications.v1.verification-code", durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.EXCHANGE_NAME, type = "direct"),
        key = "user.verification-code"
    ))
    public void sendVerificationCodeMail(VerificationCodeEvent message) {
        Context context = new Context();
        context.setVariables(Map.of(
            "fullname", message.fullName(),
            "email", message.email(),
            "code", message.code()
        ));
        String htmlContent = templateEngine.process("verification-code", context);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from(String.format("código de verificação. <%s>", resendConfig.getEmailFrom()))
            .to(message.email())
            .subject("código de confirmação.")
            .html(htmlContent)
            .build();

        try {
            resendConfig.resendClient().emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

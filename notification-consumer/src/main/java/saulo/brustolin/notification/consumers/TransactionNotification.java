package saulo.brustolin.notification.consumers;

import java.text.NumberFormat;
import java.util.Locale;
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
import saulo.brustolin.notification.models.TransactionEvent;

@Component
@AllArgsConstructor
public class TransactionNotification {

    private final ResendConfig resendConfig;
    private final TemplateEngine templateEngine;
    private final Locale locale = Locale.of("pt", "BR");;
    private final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
    
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "notifications.v1.transaction-created", durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.EXCHANGE_NAME, type = "direct"),
        key = "transaction.created"
    ))
    public void sendCreatedTransactionMail(TransactionEvent message) {
        Context context = new Context();
        context.setVariables(Map.of(
            "fullname", message.fullname(),
            "description", message.description(),
            "amount", numberFormat.format(message.amount() / 100),
            "type", message.type()
        ));
        String htmlContent = templateEngine.process("transaction-created", context);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from(String.format("criação de transação. <%s>", resendConfig.getEmailFrom()))
            .to(message.email())
            .subject("uma nova transação foi criada.")
            .html(htmlContent)
            .build();

        try {
            resendConfig.resendClient().emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "notifications.v1.transaction-updated", durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.EXCHANGE_NAME, type = "direct"),
        key = "transaction.updated"
    ))
    public void sendUpdatedTransactionMail(TransactionEvent message) {
        Context context = new Context();
        context.setVariables(Map.of(
            "fullname", message.fullname(),
            "description", message.description(),
            "amount", numberFormat.format(message.amount() / 100),
            "type", message.type()
        ));
        String htmlContent = templateEngine.process("transaction-updated", context);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from(String.format("atualização de transação. <%s>", resendConfig.getEmailFrom()))
            .to(message.email())
            .subject("uma transação existente foi atualizada.")
            .html(htmlContent)
            .build();

        try {
            resendConfig.resendClient().emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

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

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import lombok.AllArgsConstructor;
import saulo.brustolin.notification.configurations.RabbitMQConfig;
import saulo.brustolin.notification.configurations.ResendConfig;
import saulo.brustolin.shared.dtos.BudgetEvent;

@Component
@AllArgsConstructor
public class BudgetNotification {

    private final Resend resend;
    private final TemplateEngine templateEngine;
    private final Locale locale = Locale.of("pt", "BR");;
    private final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
    
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "notifications.v1.budget", durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.EXCHANGE_NAME, type = "direct"),
        key = "budget.target"
    ))
    public void sendBudgetStateMail(BudgetEvent message) {
        Context context = new Context();
        context.setVariables(Map.of(
            "fullname", message.fullname(),
            "description", message.name()
        ));

        Boolean budgetCompleted = message.balance() >= message.target();
        String htmlContent = "";
        if (budgetCompleted) {
            htmlContent = templateEngine.process("budget-completed", context);
        } else {
            htmlContent = templateEngine.process("budget-proccess", context);
        }

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from(String.format("%s <%s>", (budgetCompleted ? "parabéns, você atingiu sua meta." : "nova atualização na sua meta, você está quase lá"), ResendConfig.EMAIL_FROM))
            .to(message.email())
            .subject("atualização nas suas metas.")
            .html(htmlContent)
            .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "notifications.v1.budget", durable = "true"),
        exchange = @Exchange(value = RabbitMQConfig.EXCHANGE_NAME, type = "direct"),
        key = "budget.created"
    ))
    public void sendBudgetCreatedMail(BudgetEvent message) {
        Context context = new Context();
        context.setVariables(Map.of(
            "fullname", message.fullname(),
            "email", message.email(),
            "balance", numberFormat.format(message.balance() / 100),
            "target", numberFormat.format(message.target() / 100),
            "description", message.name()
        ));

        String htmlContent = templateEngine.process("budget-created", context);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from(String.format("um novo objetivo foi criado. <%s>", ResendConfig.EMAIL_FROM))
            .to(message.email())
            .subject("parabéns, você está a um passo de uma nova conquista.")
            .html(htmlContent)
            .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package saulo.brustolin.notification.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class ResendConfig {

    @Value("${resend.from}")
    public static String EMAIL_FROM;
    
    @Value("${resend.api.key}")
    public String apiKey;

    @Bean
    public Resend resendClient() {
        return new Resend(apiKey);
    }
}

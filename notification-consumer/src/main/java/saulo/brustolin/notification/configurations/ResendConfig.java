package saulo.brustolin.notification.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

@Configuration
public class ResendConfig {

    public static final String EMAIL_FROM = "onboarding@resend.dev";
    
    @Value("${resend.api.key}")
    public String apiKey;

    @Bean
    public Resend resendClient() {
        return new Resend(apiKey);
    }
}

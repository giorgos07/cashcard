package eu.quento.cashcard.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/cash-cards/**").hasRole("CARD-OWNER")
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder userBuilder = User.builder();

        UserDetails user1 = userBuilder
                .username("gmanoltzas@quento.eu")
                .password(passwordEncoder.encode("123abc!"))
                .roles("CARD-OWNER")
                .build();

        UserDetails user2 = userBuilder
                .username("other-user@example.com")
                .password(passwordEncoder.encode("456def!"))
                .roles("NON-OWNER")
                .build();

        UserDetails user3 = userBuilder
                .username("owner@example.com")
                .password(passwordEncoder.encode("hello123"))
                .roles("CARD-OWNER")
                .build();

        return new InMemoryUserDetailsManager(user1, user2, user3);
    }
}

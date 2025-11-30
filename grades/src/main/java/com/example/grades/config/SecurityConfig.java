package com.example.grades.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // для учебного проекта CSRF можно отключить, чтобы не править формы
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // статика и страницы регистрации/логина доступны всем
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/register", "/login").permitAll()

                        // страницы добавления/редактирования/удаления — только ADMIN
                        .requestMatchers("/grades/new", "/grades/*/edit", "/grades/*/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/grades/**").hasRole("ADMIN")

                        // просмотр списка /grades (и, если есть, /grades/{id}) — USER или ADMIN
                        .requestMatchers(HttpMethod.GET, "/grades/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/", "/home").hasAnyRole("USER", "ADMIN")

                        // всё остальное — только авторизованные
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/grades", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}



package tn.esprit.examen.springSecurity;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <-- be explicit
                .headers(h -> h
                        .crossOriginOpenerPolicy(coop -> coop
                                .policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN_ALLOW_POPUPS))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' https://accounts.google.com"))
                )
                .authorizeHttpRequests(auth -> auth
                        // let preflight through everywhere
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // your existing public matchers
                        .requestMatchers(
                                "/user/**","/user/signup","/user/login","user/forgot-password","user/reset-password",
                                "user/google-login","/user/update-profile/**","user/profile","/employer/create","/employer/**",
                                "/jobOffer/**","/jobOffer/create","/company/**","/company/create","/application/**",
                                "/application/apply","/application/byJobOffer/**","/admin/add","/admin/**","/admin/getAll",
                                "/admin/get/**","/user/upload-profile-picture/**","/ws-chat/**","/websocket/**","/info/**",
                                "/ws/**","/topic/**","/app/**","/history/**","/conversations/**","/user/update-credentials/",
                                "/upload","/uploads/**","/user/user/**","/application/accept/**","/notifications/user/**",
                                "/notifications/read/**","/favorites/add","/favorites/remove","/favorites/byCandidate/**",
                                "/admin/dashboard/overview","/candidate/all","/candidate/update/**","/candidate/delete/**",
                                "/interviews/propose","/interviews/confirm","/interviews/candidate/**","/interviews/employer/**",
                                "/interviews/cancel/**","/candidate/count","/api/support/send/**","/api/home/**",
                                "/testimonials/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowCredentials(true);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept","Origin"));
        // If you want to read any custom headers in responses:
        // cfg.setExposedHeaders(List.of("Location"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }


}


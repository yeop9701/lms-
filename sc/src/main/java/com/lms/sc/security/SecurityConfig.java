package com.lms.sc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.ignoringRequestMatchers("/userVideo/save", "/note/create/**", "/user/emailCheck"))
//			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
				.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/video/list/**")).hasRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/lecture/modify/**")).hasRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/video/addVideo/**")).hasRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
				.formLogin((formLogin) -> formLogin.loginPage("/user/login")
						.defaultSuccessUrl("/")
						.failureHandler(customAuthenticationFailureHandler()))
				.logout((logout) -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
						.logoutSuccessUrl("/").invalidateHttpSession(true))
				.exceptionHandling(exceptionHandling -> exceptionHandling
		                .accessDeniedHandler(accessDeniedHandler()))
			;
			
		return http.build();
	}

    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> response.sendRedirect("/lecture/error");
    }
	
	@Bean
    CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
}

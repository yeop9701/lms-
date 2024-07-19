package com.lms.sc.security;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
	@Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.username}")
    private String id;
    @Value("${spring.mail.password}")
    private String password;
    
    @Bean
    public JavaMailSender javaMailSender() { 
    	JavaMailSenderImpl jms = new JavaMailSenderImpl();
    	jms.setHost(host);
    	jms.setUsername(id);
    	jms.setPassword(password);
    	jms.setPort(port);
    	
    	jms.setJavaMailProperties(getMailPropertise());
    	
    	return jms;
    }
    
    private Properties getMailPropertise() {
    	Properties pro = new Properties();
    	pro.setProperty("mail.transport.protocol", "smtp");
    	pro.setProperty("mail.smtp.auth", "true");
    	pro.setProperty("mail.smtp.starttls.enable", "true");
    	pro.setProperty("mail.smtp.starttls.required", "true");
    	pro.setProperty("mail.debug", "true");
    	return pro;
    }
}

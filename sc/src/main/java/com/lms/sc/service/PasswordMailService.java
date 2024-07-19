package com.lms.sc.service;

import java.io.UnsupportedEncodingException;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.lms.sc.exception.EmailException;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordMailService {
	private final JavaMailSender jms;
	private String ePw;
	
	//임시 비밀번호를 포함한 이메일 메시지를 생성하는 메서드입니다
	public MimeMessage createMessage(String to) //to는 파라미터는 수신자의 이메일 주소를 나타냅니다.
		throws UnsupportedEncodingException, MessagingException {
		
		MimeMessage mms = jms.createMimeMessage();
			
		mms.addRecipients(RecipientType.TO, to);
		mms.setSubject("sbb 임시 비밀번호");
		
		// HTML 형식의 본문 설정
        String msgg = "<div style='margin:100px;'>" +
                      "<div align='center' style='border:1px solid black; font-family:verdana';>" +
                      "<h3 style='color:blue;'>임시 비밀번호입니다.</h3>" +
                      "<div style='font-size:130%'>" +
                      "CODE : <strong>" + ePw + "</strong><div><br/> " +
                      "</div>";
        mms.setContent(msgg, "text/html; charset=utf-8");
        //메일 발신자 설정
        mms.setFrom(new InternetAddress("jea5158@gmail.com", "sbb_Admin"));
        
        return mms;
		
	}
	//수신자의 이메일 주소와 임시 비밀번호를 받아서 이메일을 전송하는 역할을 합니다. 
	//createMessage 메서드를 호출하여 MimeMessage를 생성한 후, JavaMailSender를 통해 이를 전송합니다. 
	//전송 중에 발생한 예외는 EmailException으로 래핑되어 처리됩니다.
	public void sendSimpleMessage(String to, String pw) {
		this.ePw = pw;
		MimeMessage mms;
		
		try {
			mms = createMessage(to);
			
		}catch(UnsupportedEncodingException | MessagingException e) {
			e.printStackTrace();
			throw new EmailException("이메일 생성 에러");
		}
		
		try {
			jms.send(mms);
		}catch(MailException e) {
			e.printStackTrace();
			throw new EmailException("이메일 전송 에러");
		}
	}
}

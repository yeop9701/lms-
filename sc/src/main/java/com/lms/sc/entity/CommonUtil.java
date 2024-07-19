package com.lms.sc.entity;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Component
public class CommonUtil {
	public String markdown(String markdown) {
		Parser parser = Parser.builder().build();
		Node document = parser.parse(markdown);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(document);
	}
	
	//대소문자숫자를 임시비밀번호 문자 조합을 명시한다. 
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10; //임시 비밀번호 길이를 담당한다.
    private static final SecureRandom RANDOM = new SecureRandom(); // 랜던으로 조합을한다.
    private static String TEMP_PASSWORD;
    
    //임시 비밀번호를 생성하는 메서드
    public static String createTempPassword() {
    	//임시 비밀번호를 저장할 StringBuilder를 사용 초기는 문자열 길이만 저장
        StringBuilder tempPassword = new StringBuilder(PASSWORD_LENGTH);
        
        //문자열 길이에 맞게 루프를 돌린다
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
        	//인덱스에 랜덤으로 문자열을 넣어주고 길이가 10이 되면 루프를 나오면서
            int index = RANDOM.nextInt(CHARACTERS.length());
            //tempPassword에 어팬드 시켜준다. charAt은 인덱스의 문자열를 반환하는 역할을 한다.
            tempPassword.append(CHARACTERS.charAt(index));
        }
        //StringBuilder에 저장된 비밀번호를 문자열로 변환시켜서 반환한다.
        return tempPassword.toString();
        
        //TEMP_PASSWORD = tempPassword.toString(); // 생성된 임시 비밀번호를 변수에 저장
        //return TEMP_PASSWORD; // 생성된 임시 비밀번호 반환
    }
}

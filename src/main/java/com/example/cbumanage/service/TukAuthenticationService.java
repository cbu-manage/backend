package com.example.cbumanage.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public class TukAuthenticationService {

    public List<String> getKeys(String studentId, String studentPassword){

        URI loginUrl = URI.create(UriComponentsBuilder
                .fromUriString("https://ksc.tukorea.ac.kr/sso/login_proc.jsp?returnurl=null")
                .queryParam("internalId", studentId)
                .queryParam("internalPw", studentPassword)
                .queryParam("externalId", "")
                .queryParam("externalPw", "")
                .queryParam("gubun", "inter")
                .toUriString());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate rt = new RestTemplate();

        ResponseEntity<String> response = rt.exchange(
                loginUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null){
            return Collections.singletonList("요청이 잘못됐나봐요! 쿠키 값이 들어오지 않아요 ㅠㅠ");
        }else{
            String cookie = cookies.get(0);
            int startIndex = "KSESSIONID=".length();
            int endIndex = cookie.indexOf(';');
            return Collections.singletonList(cookie.substring(startIndex, endIndex));   //여기에 jwt 추가

        }
    }



}

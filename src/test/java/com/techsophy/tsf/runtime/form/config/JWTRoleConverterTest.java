package com.techsophy.tsf.runtime.form.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import com.techsophy.tsf.runtime.form.utils.WebClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTRoleConverterTest {

    @Mock
    HttpServletRequest mockHttpServletRequest;

    @Mock
    ObjectMapper mockObjectMapper;

    @Mock
    WebClientWrapper webClientWrapper;

    @Mock
    TokenUtils tokenUtils;

    @InjectMocks
    JWTRoleConverter jwtRoleConverter;

    @Test
    void convertTest() throws JsonProcessingException
    {
        Map<String, Object> map = new HashMap<>();
        map.put("clientRoles", "abc");
        List<String> list=new ArrayList<>();
        list.add("augmnt");
        list.add("augmnt");
        Jwt jwt= new Jwt("1", Instant.now(),null,Map.of("abc","abc"),Map.of("abc","abc"));
        WebClient webClient= WebClient.builder().build();
        when(webClientWrapper.createWebClient(any())).thenReturn(webClient);
        when(webClientWrapper.webclientRequest(any(), anyString(), anyString(), any())).thenReturn("abc");
        when(mockObjectMapper.readValue("abc",Map.class)).thenReturn(map);
        when(mockObjectMapper.convertValue("abc", List.class)).thenReturn(List.of());
        Collection grantedAuthority =  jwtRoleConverter.convert(jwt);
        Assertions.assertNotNull(grantedAuthority);
    }

    @Test
    void convertTestWhileUserInfoResponseIsEmpty() throws JsonProcessingException
    {
        Map<String, Object> map = new HashMap<>();
        map.put("clientRoles", "abc");
        List<String> list=new ArrayList<>();
        list.add("augmnt");
        list.add("augmnt");
        Jwt jwt= new Jwt("1", Instant.now(),null,Map.of("abc","abc"),Map.of("abc","abc"));
        WebClient webClient= WebClient.builder().build();
        when(webClientWrapper.createWebClient(any())).thenReturn(webClient);
        when(webClientWrapper.webclientRequest(any(), anyString(), anyString(), any())).thenReturn("");
        Assertions.assertThrows(AccessDeniedException.class, () -> jwtRoleConverter.convert(jwt));
    }

    @Test
    void convertTestWhileUserInformationIsEmpty() throws JsonProcessingException
    {
        Map<String, Object> map = new HashMap<>();
        map.put("client", "abc");
        List<String> list=new ArrayList<>();
        list.add("augmnt");
        list.add("augmnt");
        Jwt jwt= new Jwt("1", Instant.now(),null,Map.of("abc","abc"),Map.of("abc","abc"));
        WebClient webClient= WebClient.builder().build();
        when(webClientWrapper.createWebClient(any())).thenReturn(webClient);
        when(webClientWrapper.webclientRequest(any(), anyString(), anyString(), any())).thenReturn("abc");
        when(mockObjectMapper.readValue("abc",Map.class)).thenReturn(map);
        //when(mockObjectMapper.convertValue("abc", List.class)).thenReturn(List.of());
        Collection grantedAuthority =  jwtRoleConverter.convert(jwt);
        Assertions.assertNotNull(grantedAuthority);
    }

}
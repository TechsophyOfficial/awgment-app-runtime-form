package com.techsophy.tsf.runtime.form.utils;

import com.techsophy.tsf.runtime.form.dto.PaginationResponsePayload;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith({SpringExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles(TEST_ACTIVE_PROFILE)
class TokenUtilsTest
{
    @Mock
    SecurityContext securityContext;
    @Mock
    SecurityContextHolder securityContextHolder;
    @MockBean
    RelationUtils mockRelationUtils;
    @InjectMocks
    TokenUtils tokenUtils;

    @Order(2)
    @Test
    void getTokenFromContext()
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        String token = tokenUtils.getTokenFromContext();
        assertThat(token).isNull();
    }

    @Order(3)
    @Test
    void getTokenFromContextSecurityExceptionTest()
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(null);
        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> tokenUtils.getTokenFromContext());
    }

    @Order(4)
    @Test
    void getTokenFromContextException()
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> tokenUtils.getLoggedInUserId());
    }

    @Order(5)
    @Test
    void getLoggedInUserIdTest()
    {
        Mockito.when(securityContext.getAuthentication()).thenReturn(null);
        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> tokenUtils.getLoggedInUserId());
    }

    @Order(6)
    @Test
    void getTenantTest()
    {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Map<String, Object> claims;
        claims = Map.of(PREFERED_USER_NAME,TENANT);
        Jwt jwt= new Jwt(TOKEN, Instant.now(), Instant.now().plusSeconds(30), Map.of(ALG, NONE), claims);
        when(authentication.getPrincipal()).thenReturn(jwt);
        String token= tokenUtils.getLoggedInUserId();
        assertThat(token).isEqualTo(TENANT);
    }

    @Order(7)
    @Test
    void getTokenFromContextTest()
    {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        Map<String, Object> claims;
        claims = Map.of(PREFERED_USER_NAME,TENANT);
        Jwt jwt= new Jwt(TOKEN, Instant.now(), Instant.now().plusSeconds(30), Map.of(ALG, NONE), claims);
        when(authentication.getPrincipal()).thenReturn(jwt);
        Assertions.assertThrows(SecurityException.class,()-> tokenUtils.getTokenFromContext());
    }

    @Test
    void getPaginationResponsePayload()
    {
        Page page = new PageImpl(List.of("abc"));
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("abc","abc");
        list.add(map);
        PaginationResponsePayload responsePayload = tokenUtils.getPaginationResponsePayload(page,list);
        assertThat(responsePayload).isNotNull();
    }

    @Test
    void getSortBy()
    {
        String[] strings = new String[2];
        strings[0]="abc:ab";
        strings[1]="abc";
        Sort response = tokenUtils.getSortBy(strings);
        assertThat(response).isInstanceOf(Sort.class);
    }
}

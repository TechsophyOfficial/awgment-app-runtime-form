package com.techsophy.tsf.runtime.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.tsf.runtime.form.config.CustomFilter;
import com.techsophy.tsf.runtime.form.controller.impl.SequenceGeneratorImpl;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorDTO;
import com.techsophy.tsf.runtime.form.dto.SequenceGeneratorResponse;
import com.techsophy.tsf.runtime.form.service.SequenceGeneratorService;
import com.techsophy.tsf.runtime.form.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TOKEN;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static com.techsophy.tsf.runtime.form.constants.SequenceGeneratorConstants.SEQUENCE_NEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith({MockitoExtension.class})
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SequenceGeneratorImplTest
{
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtSaveOrUpdate = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_CREATE_OR_UPDATE));
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRead = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_READ));
    private static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtDelete = jwt().authorities(new SimpleGrantedAuthority(AWGMENT_RUNTIME_FORM_DELETE));

    @Mock
    SequenceGeneratorService idGeneratorService;
    @Mock
    SequenceGeneratorDTO sequenceGeneratorDTO;
    @MockBean
    TokenUtils mockTokenUtils;
    @Mock
    SequenceGeneratorResponse sequenceGeneratorResponse;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    WebApplicationContext webApplicationContext;
    @Autowired
    CustomFilter customFilter;
    @MockBean
    SequenceGeneratorService sequenceGeneratorService;
    @InjectMocks
    SequenceGeneratorImpl sequenceGenerator;

    @BeforeEach
    public void setUp()
    {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(customFilter)
                .apply(springSecurity())
                .build();
    }

    @Test
    void generateSequenceTest() throws Exception
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        Mockito.when(mockTokenUtils.getIssuerFromToken(TOKEN)).thenReturn(TENANT);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(BASE_URL+VERSION_V1+SEQUENCE_NEXT)
                .content(objectMapperTest.writeValueAsString(sequenceGeneratorDTO))
                .with(jwtSaveOrUpdate)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
    }
}

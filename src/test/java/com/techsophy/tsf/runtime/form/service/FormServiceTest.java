package com.techsophy.tsf.runtime.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techsophy.idgenerator.IdGeneratorImpl;
import com.techsophy.tsf.runtime.form.config.GlobalMessageSource;
import com.techsophy.tsf.runtime.form.dto.FormResponseSchema;
import com.techsophy.tsf.runtime.form.dto.FormSchema;
import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.entity.Status;
import com.techsophy.tsf.runtime.form.exception.EntityIdNotFoundException;
import com.techsophy.tsf.runtime.form.exception.UserDetailsIdNotFoundException;
import com.techsophy.tsf.runtime.form.repository.FormDefinitionRepository;
import com.techsophy.tsf.runtime.form.service.impl.FormServiceImpl;
import com.techsophy.tsf.runtime.form.utils.UserDetails;
import lombok.Cleanup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;
import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class FormServiceTest
{
    @Mock
    FormDefinitionRepository mockFormDefinitionRepository;
    @Mock
    MessageSource mockMessageSource;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    IdGeneratorImpl mockIdGenerator;
    @Mock
    GlobalMessageSource mockGlobalMessageSource;
    @Mock
    FormDefinition mockFormDefinition;
    @Mock
    FormDefinition mockFormio;
    @Mock
    UserDetails mockUserDetails;
    @InjectMocks
    FormServiceImpl mockFormServiceImpl;

    List<Map<String, Object>> userList = new ArrayList<>();
    List<Map<String,Object>> list=new ArrayList<>();
    List<Map<String, Object>> newUserList = new ArrayList<>();

    @BeforeEach
    public void init()
    {
        Map<String, Object> map = new HashMap<>();
        map.put(CREATED_BY_ID, NULL);
        map.put(CREATED_BY_NAME, NULL);
        map.put(CREATED_ON, NULL);
        map.put(UPDATED_BY_ID, NULL);
        map.put(UPDATED_BY_NAME, NULL);
        map.put(UPDATED_ON, NULL);
        map.put(ID, BIGINTEGER_ID);
        map.put(USER_NAME, USER_FIRST_NAME);
        map.put(FIRST_NAME, USER_LAST_NAME);
        map.put(LAST_NAME, USER_FIRST_NAME);
        map.put(MOBILE_NUMBER, NUMBER);
        map.put(EMAIL_ID, MAIL_ID);
        map.put(DEPARTMENT, NULL);
        userList.add(map);
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> mapData =new HashMap<>();
        mapData.put("create","true");
        list.add(mapData);
    }

    @Test
    void saveRuntimeFormTest() throws IOException
    {
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        FormSchema formSchemaTest =new FormSchema(TEST_ID,TEST_NAME,TEST_COMPONENTS,list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE);
        mockFormServiceImpl.saveRuntimeForm(formSchemaTest);
        verify(mockFormDefinitionRepository, times(1)).save(any());
    }

    @Test
    void saveRuntimeFormGetIsDefaultNullTest() throws IOException
    {
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(userList);
        FormSchema formSchemaTest =new FormSchema(TEST_ID,TEST_NAME,TEST_COMPONENTS,list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,null);
        mockFormServiceImpl.saveRuntimeForm(formSchemaTest);
        verify(mockFormDefinitionRepository, times(1)).save(any());
    }

    @Test
    void saveRuntimeFormEmptyIdTest() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put(CREATED_BY_ID, NULL);
        map.put(CREATED_BY_NAME, NULL);
        map.put(CREATED_ON, NULL);
        map.put(UPDATED_BY_ID, NULL);
        map.put(UPDATED_BY_NAME, NULL);
        map.put(UPDATED_ON, NULL);
        map.put(ID, EMPTY_STRING);
        map.put(USER_NAME, USER_FIRST_NAME);
        map.put(FIRST_NAME, USER_LAST_NAME);
        map.put(LAST_NAME, USER_FIRST_NAME);
        map.put(MOBILE_NUMBER, NUMBER);
        map.put(EMAIL_ID, MAIL_ID);
        map.put(DEPARTMENT, NULL);
        newUserList.add(map);

        FormSchema formSchemaTest =new FormSchema(TEST_ID,TEST_NAME,TEST_COMPONENTS,list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE);
        Mockito.when(mockUserDetails.getUserDetails()).thenReturn(newUserList);
        Assertions.assertThrows(UserDetailsIdNotFoundException.class,()->mockFormServiceImpl.saveRuntimeForm(formSchemaTest));
    }

    @Test
    void updateRuntimeFormTypFormTest() throws IOException
    {
        Mockito.when(mockUserDetails.getUserDetails())
                .thenReturn(userList);
        FormSchema formSchemaTest =new FormSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE);
        mockFormServiceImpl.saveRuntimeForm(formSchemaTest);
        verify(mockFormDefinitionRepository, times(1)).save(any());
    }

    @Test
    void updateFormTypeComponentTest() throws IOException
    {
        Mockito.when(mockUserDetails.getUserDetails())
                .thenReturn(userList);
        FormSchema formSchemaTest =new FormSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,list,TEST_PROPERTIES, TEST_TYPE_COMPONENT, TEST_VERSION,IS_DEFAULT_VALUE);
        mockFormServiceImpl.saveRuntimeForm(formSchemaTest);
        verify(mockFormDefinitionRepository, times(1)).save(any());
    }

    @Test
    void getRuntimeFormByIdTest() throws IOException
    {
        ObjectMapper objectMapper=new ObjectMapper();
        @Cleanup InputStream stream=new ClassPathResource(TEST_FORMS_DATA).getInputStream();
        String formDataTest =new String(stream.readAllBytes());
        FormResponseSchema formSchemaTest =new FormResponseSchema(TEST_ID,TEST_NAME,
                TEST_COMPONENTS,list,TEST_PROPERTIES,TEST_TYPE_FORM,TEST_VERSION,IS_DEFAULT_VALUE,TEST_CREATED_BY_ID,String.valueOf(TEST_CREATED_ON),TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON), Status.DISABLED);
        FormDefinition formDefinitionTest =objectMapper.readValue(formDataTest,FormDefinition.class);
        when(mockObjectMapper.convertValue(any(),eq(FormResponseSchema.class))).thenReturn(formSchemaTest);
        when(mockFormDefinitionRepository.findById(BigInteger.valueOf(Long.parseLong(TEST_ID)))).thenReturn(java.util.Optional.ofNullable(formDefinitionTest));
        mockFormServiceImpl.getRuntimeFormById(TEST_ID);
        verify(mockFormDefinitionRepository, times(1)).findById(BigInteger.valueOf(1));
    }

    @Test
    void getAllRuntimeFormsIncludeFormContentAndFormTest() throws IOException
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formsDataTest = new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest = objectMapperTest.readValue(formsDataTest, FormDefinition.class);
        when(mockFormDefinitionRepository.findByType(TEST_TYPE_FORM)).thenReturn(List.of(formDefinitionTest));
        mockFormServiceImpl.getAllRuntimeForms(true, TEST_TYPE_FORM);
        verify(mockFormDefinitionRepository,times(1)).findByType(TEST_TYPE_FORM);
    }

    @Test
    void getAllRuntimeFormsIncludeFormContentAndComponentTest() throws IOException
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formsData = new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest = objectMapperTest.readValue(formsData, FormDefinition.class);
        when(mockFormDefinitionRepository.findByType(TEST_TYPE_COMPONENT)).thenReturn(List.of(formDefinitionTest));
        mockFormServiceImpl.getAllRuntimeForms(true, TEST_TYPE_COMPONENT);
        verify(mockFormDefinitionRepository,times(1)).findByType(TEST_TYPE_COMPONENT);
    }

    @Test
    void getAllRuntimeFormsIncludeFormContentAndNoTypeTest() throws IOException
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formsDataTest = new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest = objectMapperTest.readValue(formsDataTest, FormDefinition.class);
        when(mockFormDefinitionRepository.findAll()).thenReturn(List.of(formDefinitionTest));
        mockFormServiceImpl.getAllRuntimeForms(true, null);
        verify(mockFormDefinitionRepository,times(1)).findAll();
    }

    @Test
    void getAllRuntimeFormsNoFormContentAndNoTypeTest() throws IOException
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formData = new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest = objectMapperTest.readValue(formData, FormDefinition.class);
        when(mockFormDefinitionRepository.findAll()).thenReturn(List.of(formDefinitionTest));
        mockFormServiceImpl.getAllRuntimeForms(false, null );
        verify(mockFormDefinitionRepository,times(1)).findAll();
    }

    @Test
    void getAllRuntimeFormsNoTypeNoContentTest() throws IOException
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formData = new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest = objectMapperTest.readValue(formData, FormDefinition.class);
        when(mockFormDefinitionRepository.findAll()).thenReturn(List.of(formDefinitionTest));
        mockFormServiceImpl.getAllRuntimeForms(false, null );
        verify(mockFormDefinitionRepository,times(1)).findAll();
    }

    @Test
    void getAllRuntimeFormsNoFormContentAndTypeTest() throws IOException
    {
        ObjectMapper objectMapperTest = new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formData = new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest = objectMapperTest.readValue(formData, FormDefinition.class);
        when(mockFormDefinitionRepository.findByType(TEST_TYPE_FORM)).thenReturn(List.of(formDefinitionTest));
        mockFormServiceImpl.getAllRuntimeForms(false, TEST_TYPE_FORM );
        verify(mockFormDefinitionRepository,times(1)).findByType(TEST_TYPE_FORM);
    }

    @Test
    void deleteRuntimeFormByIdTest()
    {
        Map<String, Object> component = new HashMap<>();
        component.put("key","value");
        FormResponseSchema formResponseSchema = new FormResponseSchema(TEST_ID, TEST_NAME, TEST_COMPONENTS,
                list,TEST_PROPERTIES,TEST_TYPE_FORM, TEST_VERSION,IS_DEFAULT_VALUE,TEST_CREATED_BY_ID
                ,String.valueOf(TEST_CREATED_ON), TEST_UPDATED_BY_ID,String.valueOf(TEST_UPDATED_ON),Status.DISABLED);
        FormDefinition formDefinition = new FormDefinition();
        formDefinition.setId(BigInteger.ONE);
        formDefinition.setName(NAME);
        formDefinition.setVersion(BigInteger.ONE);
        formDefinition.setComponents(component);
        formDefinition.setAcls(List.of(component));
        formDefinition.setProperties(component);
        formDefinition.setType(TYPE);
        formDefinition.setIsDefault(true);
        when(mockFormDefinitionRepository.existsById(BigInteger.valueOf(1))).thenReturn(true);
        when(mockFormDefinitionRepository.findById(BigInteger.valueOf(1))).thenReturn(Optional.of(formDefinition));
        when(mockObjectMapper.convertValue(any(),eq(FormResponseSchema.class))).thenReturn(formResponseSchema);
        when(mockFormDefinitionRepository.deleteById(BigInteger.valueOf(1))).thenReturn(Integer.valueOf(TEST_ID));
        mockFormServiceImpl.deleteRuntimeFormById(TEST_ID);
        verify(mockFormDefinitionRepository, times(1)).deleteById(BigInteger.valueOf(1));
    }

    @Test
    void deleteRuntimeFormByIdExceptionTest()
    {
        when(mockFormDefinitionRepository.existsById(BigInteger.valueOf(1))).thenReturn(false);
        Assertions.assertThrows(EntityIdNotFoundException.class,()->mockFormServiceImpl.deleteRuntimeFormById(TEST_ID));
    }

    @Test
    void searchRuntimeFormByIdOrNameLike() throws IOException
    {
        ObjectMapper objectMapperTest=new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formData= new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest =objectMapperTest.readValue(formData,FormDefinition.class);
        when(mockFormDefinitionRepository.findByNameOrIdAndType(TEST_ID_OR_NAME_LIKE, TEST_TYPE_FORM)).thenReturn(Collections.singletonList(formDefinitionTest));
        mockFormServiceImpl.searchRuntimeFormByIdOrNameLike(TEST_ID_OR_NAME_LIKE, TEST_TYPE_FORM);
        verify(mockFormDefinitionRepository, times(1)).findByNameOrIdAndType(TEST_ID_OR_NAME_LIKE, TEST_TYPE_FORM);
    }

    @Test
    void searchRuntimeFormByIdOrNameLikeTypeNullTest() throws IOException
    {
        ObjectMapper objectMapperTest=new ObjectMapper();
        @Cleanup InputStream inputStreamTest = new ClassPathResource(TEST_FORMS_DATA_1).getInputStream();
        String formData= new String(inputStreamTest.readAllBytes());
        FormDefinition formDefinitionTest =objectMapperTest.readValue(formData,FormDefinition.class);
        when(mockFormDefinitionRepository.findByNameOrId(TEST_ID_OR_NAME_LIKE)).thenReturn(Collections.singletonList(formDefinitionTest));
        mockFormServiceImpl.searchRuntimeFormByIdOrNameLike(TEST_ID_OR_NAME_LIKE, null);
        verify(mockFormDefinitionRepository,times(1)).findByNameOrId(TEST_ID_OR_NAME_LIKE);
    }
}

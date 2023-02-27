package com.techsophy.tsf.runtime.form.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.techsophy.tsf.runtime.form.constants.RuntimeFormTestConstants.TEST_ACTIVE_PROFILE;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ActiveProfiles(TEST_ACTIVE_PROFILE)
@ExtendWith(MockitoExtension.class)
class LocaleConfigTest
{
    @Mock
    HttpServletRequest mockHttpServletRequest;
    @Mock
    List<Locale> mockLocales;
    @Mock
    List<Locale.LanguageRange> mockList;
    @InjectMocks
    LocaleConfig mockLocaleConfig;

    @Test
     void resolveLocaleTest()
    {
        when(mockLocaleConfig.resolveLocale(mockHttpServletRequest)).thenReturn(any());
        Locale responseTest= mockLocaleConfig.resolveLocale(mockHttpServletRequest);
        Assertions.assertNotNull(responseTest);
    }

    @Test
    void resolveLocaleAcceptHeadersTest()
    {
        Mockito.when(mockHttpServletRequest.getHeader(any())).thenReturn("eu");
        when(mockLocaleConfig.resolveLocale(mockHttpServletRequest)).thenReturn(any());
        Locale responseTest= mockLocaleConfig.resolveLocale(mockHttpServletRequest);
        Assertions.assertNotNull(responseTest);
    }
}

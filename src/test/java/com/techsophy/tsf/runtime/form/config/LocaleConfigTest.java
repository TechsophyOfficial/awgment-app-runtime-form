package com.techsophy.tsf.runtime.form.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

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

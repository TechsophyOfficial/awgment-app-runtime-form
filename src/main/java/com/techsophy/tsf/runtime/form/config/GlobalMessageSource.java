package com.techsophy.tsf.runtime.form.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = {@Autowired})
@NoArgsConstructor
public class GlobalMessageSource
{
    private MessageSource messageSource;

    public String get(String key)
    {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public String get(String key,String args)
    {
        return messageSource.getMessage(key, new Object[]{args}, LocaleContextHolder.getLocale());
    }
}

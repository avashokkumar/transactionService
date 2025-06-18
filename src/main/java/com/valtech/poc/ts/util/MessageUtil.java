package com.valtech.poc.ts.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageUtil {

    @Autowired
    private MessageSource messageSource;

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault());
    }
}
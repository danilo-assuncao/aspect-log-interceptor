package com.dassuncao.aspect.log.interceptor;

import com.dassuncao.aspect.log.interceptor.annotation.LogParameter;
import com.dassuncao.aspect.log.interceptor.annotation.Loggable;

public class MethodSample {

    @Loggable(logError = false)
    public String method(@LogParameter final String name, @LogParameter final String lastName) {
        return "PARAMETER RECEIVES IS = " + name + " " + lastName;
    }
}

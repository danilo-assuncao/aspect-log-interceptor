package com.dassuncao.aspect.log.interceptor;

import com.dassuncao.aspect.log.interceptor.annotation.LogParameter;
import com.dassuncao.aspect.log.interceptor.annotation.Loggable;

public class MethodSample {

    @Loggable
    public String method(@LogParameter final String name) {
        return "PARAMETER RECEIVES IS = " + name;
    }
}
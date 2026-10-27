package com.assethealth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {

    @GetMapping(value = {"/overview", "/overview/**", "/transformer", "/transformer/**", "/quality", "/quality/**"})
    public String forwardToApp() {
        return "forward:/index.html";
    }
}

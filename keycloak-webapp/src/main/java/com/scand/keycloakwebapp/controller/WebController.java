package com.scand.keycloakwebapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping(path = "/default")
    public String indexDefault() {
        return "redirect:/default/index.html";
    }

    @GetMapping(path = "/custom")
    public String indexCustom() {
        return "redirect:/custom/index.html";
    }
}

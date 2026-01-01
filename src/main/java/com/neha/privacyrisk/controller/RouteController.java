package com.neha.privacyrisk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouteController {

    // This controller handles client-side routing by forwarding 404s to index.html
    @org.springframework.web.bind.annotation.RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }

    // Support nested routes
    @GetMapping("/**/{path:[^\\.]*}")
    public String redirectNested() {
        return "forward:/index.html";
    }
}

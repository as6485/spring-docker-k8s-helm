package org.ayan.springdockerk8shelm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
    @GetMapping("/hello")
    public String helloWorldController() {
        return "Hello World!";
    }
}

package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

// Controller requires '@Controller' annotation.
@Controller
public class RootController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root(@RequestParam("name") String name, Model model) {
        model.addAttribute("name", "name");
        return "index";
    }
}

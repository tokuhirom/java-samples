package com.example.controller;

import com.example.setting.PhaseSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class CustomErrorController extends AbstractErrorController {
    @Autowired
    private PhaseSetting phaseSetting;

    private static final String PATH = "/error";

    public CustomErrorController() {
        super(new DefaultErrorAttributes());
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(PATH)
    public String error(
            HttpServletRequest request,
            Model model
    ) {
        if (phaseSetting.isLocal()) {
            final Map<String, Object> errorAttributes = getErrorAttributes(request, true);
            model.addAttribute("error", errorAttributes);
            return "error_local_for_development";
        } else {
            return "error";
        }
    }
}

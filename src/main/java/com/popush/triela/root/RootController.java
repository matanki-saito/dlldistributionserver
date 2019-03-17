package com.popush.triela.root;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {
    @GetMapping("/")
    public String exeRegisterGet(
            Model model
    ) {
        return "root";
    }
}

package com.popush.triela.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SampleController {
    @GetMapping("/sample")
    public String distributionGet(Model model) {
        return "sample";
    }
}

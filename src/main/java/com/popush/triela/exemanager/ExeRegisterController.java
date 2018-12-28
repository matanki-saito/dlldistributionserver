package com.popush.triela.exemanager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mgr/v1")
public class ExeRegisterController {
    private final ExeRegisterService exeRegisterService;

    @GetMapping("/exeRegister")
    public String exeRegisterGet(ExeRegisterForm exeRegisterForm, Model model) {
        model.addAttribute("form", exeRegisterForm);
        model.addAttribute("exeList", exeRegisterService.list());

        return "exeRegister";
    }

    @PostMapping("/exeRegister")
    public String exeRegisterPost(@Validated ExeRegisterForm exeRegisterForm, Model model) {
        exeRegisterService.save(exeRegisterForm);

        model.addAttribute("form", exeRegisterForm);
        model.addAttribute("exeList", exeRegisterService.list());

        return "redirect:exeRegister";
    }
}

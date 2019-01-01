package com.popush.triela.Manager.product;

import com.popush.triela.Manager.TrielaManagerV1Controller;
import com.popush.triela.common.Exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProductController extends TrielaManagerV1Controller {

    private final ProductService productService;

    @GetMapping("/product")
    public String exeRegisterGet(
            ProductForm productForm,
            Model model
    ) throws ServiceException {

        model.addAttribute("form", productForm);
        model.addAttribute("list", productService.list());

        return "product";
    }
}

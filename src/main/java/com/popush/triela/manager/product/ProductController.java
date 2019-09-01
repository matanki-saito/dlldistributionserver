package com.popush.triela.manager.product;

import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.manager.TrielaManagerV1Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProductController extends TrielaManagerV1Controller {

    private final ProductService productService;

    @GetMapping("/product")
    public String exeRegisterGet(
            ProductSearchConditionForm condition,
            Model model,
            Pageable pageable
    ) throws OtherSystemException {

        model.addAttribute("form", productService.page(condition, pageable));
        model.addAttribute("condition", condition);
        return "product";
    }
}

package com.popush.triela.Manager.GoogleDrive;

import com.popush.triela.Manager.TrielaManagerV1Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class GoogleDriveController extends TrielaManagerV1Controller {

    private final OAuth2RestTemplate restTemplate;

    @GetMapping("product/googleDrive")
    public String googleDriveView(
            Model model
    ) {
        return restTemplate.getAccessToken().getValue();
    }

}

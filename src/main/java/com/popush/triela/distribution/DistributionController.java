package com.popush.triela.distribution;

import com.popush.triela.exeregister.ExeRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DistributionController {
    //private final OAuth2RestTemplate auth2RestTemplate;

    private final DistributionService distributionService;
    private final ExeRegisterService exeRegisterService;

    @GetMapping("/distribution")
    public String distributionGet(Model model) {
        //userService.login(auth2RestTemplate.getResource().getClientId());

        //auth2RestTemplate.getResource().getClientId();

        //URI uri = UriComponentsBuilder.fromUriString("https://api.github.com/user/repos").build().toUri();
        //model.addAttribute("repos", auth2RestTemplate.getForEntity(uri, GitHubRepoDto[].class).getBody());

        model.addAttribute("assetList", distributionService.list());
        model.addAttribute("exeRegisterList", exeRegisterService.list());

        return "distribution";
    }


    @PostMapping("/distribution")
    public String distributionPost(@RequestParam MultiValueMap<String, String> params,Model model) {

        /* exe-hash:asset-id */
        final Map<String,Integer> m = params.entrySet().stream()
                .filter(entry-> !entry.getKey().equals("_csrf"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e->Integer.parseInt(e.getValue().get(0))
                ));

        distributionService.update(m,"eu4dll");

        return "redirect:distribution";
    }
}

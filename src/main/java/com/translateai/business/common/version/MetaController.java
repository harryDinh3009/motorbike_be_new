package com.translateai.business.common.version;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MetaController {

    @GetMapping("/meta")
    public String showMetaPage() {
        return "meta";
    }

}


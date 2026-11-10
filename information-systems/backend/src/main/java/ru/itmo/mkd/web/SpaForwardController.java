package ru.itmo.mkd.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/", "/{path:^(?!api|assets)[^.]*}", "/{path:^(?!api|assets)[^.]*}/**"})
    public String forward() {
        return "forward:/index.html";
    }
}

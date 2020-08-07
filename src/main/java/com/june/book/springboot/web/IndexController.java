package com.june.book.springboot.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(){
        return "index"; // src/main/resources/templates/index.mustache
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }
}

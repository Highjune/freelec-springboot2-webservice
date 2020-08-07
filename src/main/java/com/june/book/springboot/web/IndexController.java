package com.june.book.springboot.web;

import com.june.book.springboot.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;

    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("posts", postsService.findAllDesc());
        return "index"; // src/main/resources/templates/index.mustache
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }
}

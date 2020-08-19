package com.june.book.springboot.web;

import com.june.book.springboot.config.auth.LoginUser;
import com.june.book.springboot.config.auth.dto.SessionUser;
import com.june.book.springboot.service.PostsService;
import com.june.book.springboot.web.dto.PostsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user){
        model.addAttribute("posts", postsService.findAllDesc());

        if (user != null){ //세션이 저장된 값이 있을 때만 저장. 값이 없으면 model에 값 없으니 로그인 버튼이 보임
            model.addAttribute("userName", user.getName());
        }
        return "index"; // src/main/resources/templates/index.mustache
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model) {
        PostsResponseDto dto = postsService.findById(id);
        model.addAttribute("post", dto);

        return "posts-update";
    }
}

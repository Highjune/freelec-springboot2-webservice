package com.june.book.springboot.web;

import com.june.book.springboot.config.auth.SecurityConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class) //JUnit에 내장된 실행자 외에 SpringRunner라는 실행자 사용(스프링 부트 테스트와 JUnit사이에 연결자 역할)
@WebMvcTest(controllers = HelloController.class,
        excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
        )
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc; //스프링 MVC 테스트의 시작점. 이 클래스를 통해 HTTP GET, POST 등에 대한 API 테스트 가능

    @WithMockUser(roles="USER")
    @Test
    public void hello가_리턴된다() throws Exception{
        String hello = "hello";

        mvc.perform(get("/hello")) //MockMvc를 통해 /hello 주소로 HTTP GET 요청하는 것
                .andExpect(status().isOk()) //요청에 대한 결과. 200인지
                .andExpect(content().string(hello));
    }

    @WithMockUser(roles="USER")
    @Test
    public void helloDto가_리턴된다() throws Exception{
        String name = "hello";
        int amount = 1000;

        mvc.perform( //param은 string만 허용되기 때문에 문자열로 변경해야 한다. 그래서 valueOf로 변환
                get("/hello/dto").param("name", name).param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.amount", is(amount)));
    }
}
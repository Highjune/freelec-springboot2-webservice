package com.june.book.springboot.service;

import com.june.book.springboot.domain.posts.Posts;
import com.june.book.springboot.domain.posts.PostsRepository;
import com.june.book.springboot.web.dto.PostsListResponseDto;
import com.june.book.springboot.web.dto.PostsResponseDto;
import com.june.book.springboot.web.dto.PostsSaveRequestDto;
import com.june.book.springboot.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostsRepository postsRepository;

    @Transactional
    public long save(PostsSaveRequestDto requestDto) {
        return postsRepository.save(requestDto.toEntity()).getId(); //save는 jpa에 의해 자동생성
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto) {
        Posts posts = postsRepository.findById(id) //findById는 jpa에 의해 자동생성
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));

        posts.update(requestDto.getTitle(), requestDto.getContent());

        return id;
    }

    @Transactional
    public void delete(Long id) {
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id = " + id));

        postsRepository.delete(posts);
    }

    @Transactional(readOnly = true)
    public PostsResponseDto findById (Long id){
        Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id= " + id));

        return new PostsResponseDto(entity); //PostsResponseDto 생성만 해주고 entity 넘겨줌
    }

    @Transactional(readOnly = true) //조회기능만 있는 경우는 이런식으로 readonly=true로 주면 조회속도가 개선(조회 기능만 갖고 있으므로)
    public List<PostsListResponseDto> findAllDesc(){
        return postsRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new) //람다임 .map(posts -> new PostsListResponseDto(posts))와 같다
                .collect(Collectors.toList());
    }
}

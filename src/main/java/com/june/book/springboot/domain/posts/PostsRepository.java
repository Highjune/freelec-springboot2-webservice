package com.june.book.springboot.domain.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//SpringDataJpa에서 기본적으로 제공하지 않는 것들은 이렇게 @query로 쓸 수 있다.
//@org.springframework.stereotype.Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {
    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    List<Posts> findAllDesc();
}


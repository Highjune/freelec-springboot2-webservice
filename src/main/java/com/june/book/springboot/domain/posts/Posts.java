package com.june.book.springboot.domain.posts;

import com.june.book.springboot.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@NoArgsConstructor
@Entity //테이블과 링크될 클래스임을 알려주는 어노테이션
public class Posts extends BaseTimeEntity { //Posts클래스는 실제 테이블과 매칭될 클래스

    @Id //해당 테이블의 PK값을 나타낸다
    @GeneratedValue(strategy= GenerationType.IDENTITY) //auto_increment
    private Long id;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String author;

    @Builder //해당 클래스의 빌더 패턴 클래스를 생성
    public Posts(String title, String content, String author){
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }
}

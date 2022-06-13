package com.sparta.week6project.controller;

import com.sparta.week6project.dto.requestDto.PostRequestDto;
import com.sparta.week6project.dto.requestDto.TagRequestDto;
import com.sparta.week6project.dto.responseDto.PostResponseDto;
import com.sparta.week6project.security.UserDetailsImpl;
import com.sparta.week6project.service.PostService;
import com.sparta.week6project.service.S3Service;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class PostController {

    private final PostService postService;
    private final S3Service s3Service;

    public PostController(PostService postService, S3Service s3Service){
        this.postService = postService;
        this.s3Service = s3Service;
    }


    // 게시글 조회
    @GetMapping("/posts/post/{postId}")
    public PostResponseDto getPost(@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.getPost(postId, userDetails.getUser().getId());
    }


    // 게시글 전체 조회
    @GetMapping("/posts")
    public List<PostResponseDto> getPosts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.getPosts(userDetails.getUser().getId());
    }


    // 작성글 전체 조회
    @GetMapping("/posts/myposts")
    public List<PostResponseDto> getMyposts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.getMyPosts(userDetails.getUser().getId());
    }


    // 좋아요한 게시글 전체 조회
    @GetMapping("/posts/heart")
    public List<PostResponseDto> getLiedPosts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.getLikedPosts(userDetails.getUser().getId());
    }


    // 같은 종류 태그 전체 조회
    @GetMapping("/posts/tag")
    public List<PostResponseDto> getTaggedPosts(@RequestBody TagRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.getTaggedPosts(userDetails.getUser().getId(), requestDto);
    }

    // ================================ 조회 컨트롤러 종료 ===============================


    // 게시글 작성
    @PostMapping("/posts/post")
    public void createBoard(@RequestBody PostRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        postService.createPost(userDetails.getUser().getId(), requestDto);
    }


    // 게시글 수정
    @PutMapping("/posts/post/{postId}")
    public void updateBoard(@PathVariable Long postId, @RequestBody PostRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        postService.updatePost(postId, userDetails.getUser().getId(), requestDto);
    }


    // 게시글 삭제
    @DeleteMapping("/posts/post/{postId}")
    public void deleteBoard(@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        postService.deletePost(postId, userDetails.getUser().getId());
    }


    // 파일 업로드 테스트
    @PostMapping("/upload")
    public String upload(@RequestPart("file") MultipartFile file){
        return s3Service.upload(file);
    }

}

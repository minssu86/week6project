package com.sparta.week6project.service;

import com.sparta.week6project.dto.requestDto.PagesRequestDto;
import com.sparta.week6project.dto.requestDto.PostRequestDto;
import com.sparta.week6project.dto.requestDto.TagRequestDto;
import com.sparta.week6project.dto.responseDto.PostResponseDto;
import com.sparta.week6project.model.Heart;
import com.sparta.week6project.model.Post;
import com.sparta.week6project.model.Tag;
import com.sparta.week6project.model.User;
import com.sparta.week6project.repository.*;
import com.sparta.week6project.repository.mapping.PostMapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final S3Service s3Service;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final HeartRepository heartRepository;
    private final CommentRepository commentRepository;
    public PostService(

            S3Service s3Service,

            PostRepository postRepository,
            UserRepository userRepository,
            TagRepository tagRepository,
            HeartRepository heartRepository,
            CommentRepository commentRepository){

        this.s3Service = s3Service;

        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.heartRepository = heartRepository;
        this.commentRepository = commentRepository;
    }


    // 게시글 조회
    public PostResponseDto getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new NullPointerException("해당 글을 찾을 수 없습니다.")
        );
        return postResMapping(post, userId);
    }


    // 게시글 전체 조회
    public List<PostResponseDto> getPosts(Long userId) {
        List<Post> posts = postRepository.findAllByOrderByModifiedAtDesc();
        return postListProcess(posts, userId);
    }

    // [10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
    // size = 3 : page = 0 [10, 9, 8], page = 1 [7, 6, 5], page = 2 [4, 3, 2], page = 3 [1]

    // [11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
    // size = 3 : page = 0 [11, 10, 9], page = 1 [8, 7, 6], page = 2 [5, 4, 3], page = 3 [2, 1]


    // Infinite Scrolling Pagination
    public List<PostResponseDto> getPostsPages(Long userId, PagesRequestDto requestDto) {

        PageRequest sortedByModifiedAtDesc = PageRequest.of(requestDto.getPage(), requestDto.getSize(), Sort.by("modifiedAt").descending()); // 페이지 정렬 셋팅
        List<Post> posts = postRepository.findAllByOrderByModifiedAtDesc(sortedByModifiedAtDesc).getContent();

        return postListProcess(posts, userId);
    }


    // 작성글 전체 조회
    public List<PostResponseDto> getMyPosts(Long userId) {
        List<Post> posts = postRepository.findAllByUserIdOrderByModifiedAtDesc(userId);
        return postListProcess(posts, userId);
    }


    // 좋아요한 게시글 전체 조회
    public List<PostResponseDto> getLikedPosts(Long userId) {
        List<PostMapping> posts = heartRepository.findAllByUserIdAndIsheartTrue(userId); // 좋아요한 게시글 불러오기
        return postMappingListProcess(posts, userId);
    }


    // 같은 종류 태그 전체 조회
    public List<PostResponseDto> getTaggedPosts(Long userId, TagRequestDto requestDto) {
        List<PostMapping> posts = tagRepository.findAllByTag(requestDto.getTag());
        return postMappingListProcess(posts, userId);
    }


    // 리스트 조회용 middle process (Post 타입)
    private List<PostResponseDto> postListProcess(List<Post> posts, Long userId){
        List<PostResponseDto> postResponseDtos = new ArrayList<>();
        for(Post post : posts){
            postResponseDtos.add(postResMapping(post, userId));
        }
        return postResponseDtos;
    }


    // 리스트 조회용 middle process (PostMapping 타입)
    private List<PostResponseDto> postMappingListProcess(List<PostMapping> posts, Long userId){
        List<PostResponseDto> postResponseDtos = new ArrayList<>();
        for(PostMapping post : posts){
            postResponseDtos.add(postResMapping( post.getPost(), userId));
        }
        return postResponseDtos;
    }


    // 게시글 조회용 postResponsMapping process
    private PostResponseDto postResMapping(Post post, Long userId){
        Heart heart;
        if(userId == 0L){
            heart = null;
        } else {
            heart = heartRepository.findByPostIdAndUserId(post.getId(), userId);
        }

        return PostResponseDto.builder()
                .id(post.getId())
                .username(post.getUser().getUsername())
                .nickname(post.getUser().getNickname())
                .title(post.getTitle())
                .contents(post.getContent())
                .imageUrl(post.getImageUrl())
                .modifiedAt(post.getModifiedAt())
                .heart(heartRepository.countByPostIdAndIsheartTrue(post.getId()))
                .isHeart(heart != null && heart.getIsheart())
                .tags(tagRepository.findAllByPostId(post.getId()))
                .build();
    }


    // ================================ 조회 메서드 종료 ===============================


    // 게시글 작성
    public void createPost(Long userId, PostRequestDto requestDto, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(
                ()-> new IllegalArgumentException("등록되지 않은 사용자입니다.")
        );

        if(file.isEmpty()){
            requestDto.setImageUrl(null);
            requestDto.setFileName(null);
        } else {
            requestDto.setImageUrlAndFileName(s3Service.upload(file));
        }

        Post post = postRepository.save(new Post(user, requestDto));
        if(!requestDto.getTags().isEmpty()) {
            for(TagRequestDto tagRequestDto : requestDto.getTags()){
                tagRepository.save(new Tag(post, tagRequestDto));
            }
        }

    }


    // 게시글 수정
    @Transactional
    public void updatePost(Long postId, Long userId, PostRequestDto requestDto, MultipartFile file) {
        Post post = postRepository.findByIdAndUserId(postId, userId);
        if(post == null){
            throw new NullPointerException("존재하지 않는 글입니다.");
        }
        // 새 파일 등록
        if(!file.isEmpty()) {
            // 기존 Url 삭제
            s3Service.deleteImageUrl(post.getFileName());
            requestDto.setImageUrlAndFileName(s3Service.upload(file));
        } else {
            requestDto.setImageUrlAndFileName(post.getImageUrl(),post.getFileName());
        }


        // DB 업데이트
        post.update(requestDto);

        // 태그 초기화 후 새로 등록
        if(requestDto.getTags().size()!=0){
            tagRepository.deleteAllByPostId(postId);
            for(TagRequestDto tagRequestDto : requestDto.getTags()){
                tagRepository.save(new Tag(post, tagRequestDto));
            }
        }

    }


    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndUserId(postId, userId);
        if(post == null ){
            throw new IllegalArgumentException("해당 글이 존재 하지 않거나 권한이 없습니다.");
        }

        // 이미지 Url 삭제
        s3Service.deleteImageUrl(post.getFileName());

        // 게시글 삭제전 게시글 DB를 참조한 하위 데이터들 먼저 삭제
        tagRepository.deleteAllByPostId(postId);
        heartRepository.deleteAllByPostId(postId);
        commentRepository.deleteAllByPostId(postId);

        // 게시글 삭제
        postRepository.deleteById(postId);

    }



}

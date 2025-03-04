package com.project.foradhd.domain.board.web.controller;

import com.project.foradhd.domain.board.persistence.entity.Comment;
import com.project.foradhd.domain.board.business.service.CommentService;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.board.web.dto.request.CreateCommentRequestDto;
import com.project.foradhd.domain.board.web.dto.response.CommentListResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostListResponseDto;
import com.project.foradhd.domain.board.web.mapper.CommentMapper;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.global.AuthUserId;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final UserService userService;

    // 개별 댓글 조회 API
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentListResponseDto.CommentResponseDto> getComment(
            @PathVariable Long commentId,
            @AuthUserId String userId) {

        Comment comment = commentService.getComment(commentId);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        return ResponseEntity.ok(
                commentMapper.commentToCommentResponseDto(comment, blockedUserIdList, userId, userService)
        );
    }

    // 댓글 작성 API
    @PostMapping
    public ResponseEntity<CommentListResponseDto.CommentResponseDto> createComment(
            @RequestBody CreateCommentRequestDto createCommentRequestDto,
            @AuthUserId String userId) {

        Comment comment = commentMapper.createCommentRequestDtoToComment(createCommentRequestDto, userId);
        Comment createdComment = commentService.createComment(comment, userId);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                commentMapper.commentToCommentResponseDto(createdComment, blockedUserIdList, userId, userService) // ✅ userService 추가
        );
    }

    // 원댓글 삭제 API
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // 개별 대댓글 삭제 API
    @DeleteMapping("/{parentId}/children/{commentId}")
    public ResponseEntity<Void> deleteChildrenComment(@PathVariable Long commentId) {
        commentService.deleteChildrenComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 수정 api
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentListResponseDto.CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CreateCommentRequestDto createCommentRequest,
            @AuthUserId String userId) {

        Comment updatedComment = commentService.updateComment(
                commentId,
                createCommentRequest.getContent(),
                createCommentRequest.isAnonymous(),
                userId);

        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        return ResponseEntity.ok(
                commentMapper.commentToCommentResponseDto(updatedComment, blockedUserIdList, userId, userService) // ✅ userService 추가
        );
    }

    // 나의 댓글
    @GetMapping("/my-comments")
    public ResponseEntity<PostListResponseDto> getMyCommentedPosts(
            @AuthUserId String userId,
            Pageable pageable,
            @RequestParam(defaultValue = "NEWEST_FIRST") SortOption sortOption) {
        Page<PostListResponseDto.PostResponseDto> posts = commentService.getMyCommentedPosts(userId, pageable, sortOption);
        List<PostListResponseDto.PostResponseDto> postList = posts.getContent();

        PagingResponse pagingResponse = PagingResponse.from(posts);

        PostListResponseDto response = PostListResponseDto.builder()
                .postList(postList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }
    // 글별 댓글 모아보기
    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommentListResponseDto> getCommentsByPost(
            @PathVariable Long postId,
            Pageable pageable,
            @RequestParam(required = false) SortOption sortOption,
            @AuthUserId String userId) {

        if (sortOption == null) {
            sortOption = SortOption.NEWEST_FIRST;
        }

        Page<Comment> comments = commentService.getCommentsByPost(postId, pageable, sortOption);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        List<CommentListResponseDto.CommentResponseDto> commentList = comments.getContent().stream()
                .map(comment -> commentMapper.commentToCommentResponseDto(comment, blockedUserIdList, userId, userService))
                .toList();

        PagingResponse pagingResponse = PagingResponse.from(comments);

        CommentListResponseDto response = CommentListResponseDto.builder()
                .commentList(commentList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }


    // 댓글 좋아요 토글
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> toggleCommentLike(@PathVariable Long commentId, @AuthUserId String userId) {
        commentService.toggleCommentLike(commentId, userId);
        return ResponseEntity.ok().build();
    }
}

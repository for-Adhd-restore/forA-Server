package com.project.foradhd.domain.board.web.controller;

import com.project.foradhd.domain.board.business.service.PostLikeFilterService;
import com.project.foradhd.domain.board.business.service.PostReportService;
import com.project.foradhd.domain.board.business.service.PostScrapFilterService;
import com.project.foradhd.domain.board.business.service.PostSearchHistoryService;
import com.project.foradhd.domain.board.business.service.PostService;
import com.project.foradhd.domain.board.business.service.dto.in.ReportPostData;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.entity.PostScrapFilter;
import com.project.foradhd.domain.board.persistence.entity.ReportPost;
import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.domain.board.persistence.enums.HandleReport;
import com.project.foradhd.domain.board.persistence.enums.Report;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.board.web.dto.request.PostRequestDto;
import com.project.foradhd.domain.board.web.dto.request.ReportTypeDto;
import com.project.foradhd.domain.board.web.dto.response.PostListResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostRankingResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostReportListResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostScrapFilterResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostSearchResponseDto;
import com.project.foradhd.domain.board.web.mapper.PostMapper;
import com.project.foradhd.domain.board.web.mapper.PostScrapFilterMapper;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.global.AuthUserId;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;
    private final PostScrapFilterService postScrapFilterService;
    private final PostScrapFilterMapper postScrapFilterMapper;
    private final PostLikeFilterService postLikeFilterService;
    private final PostSearchHistoryService postSearchHistoryService;
    private final UserService userService;
    private final PostReportService postReportService;

    // 게시글 개별 조회 api
    @GetMapping("/{postId}")
    public ResponseEntity<PostListResponseDto.PostResponseDto> getPost(
            @PathVariable Long postId, @AuthUserId String userId) {

        Post post = postService.getPost(postId);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        boolean isScrapped = postScrapFilterService.isUserScrappedPost(userId, postId);
        boolean isLiked = postLikeFilterService.isUserLikedPost(userId, postId);
        boolean isAuthor = post.getUser().getId().equals(userId);

        return ResponseEntity.ok(
                postMapper.toPostResponseDto(post, userService, blockedUserIdList, isScrapped, isLiked, isAuthor, userId)
        );
    }

    // 게시글 작성 api
    @PostMapping
    public ResponseEntity<PostListResponseDto.PostResponseDto> createPost(
            @RequestBody PostRequestDto postRequestDto, @AuthUserId String userId) {

        Post post = postMapper.toEntity(postRequestDto, userId, userService);
        Post createdPost = postService.createPost(post);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postMapper.toPostResponseDto(createdPost, userService, blockedUserIdList, false, false, true, userId));
    }

    // 게시글 수정 api
    @PutMapping("/{postId}")
    public ResponseEntity<PostListResponseDto.PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequestDto postRequestDto,
            @AuthUserId String userId) {

        Post existingPost = postService.getPost(postId);
        Post updatedPost = existingPost.toBuilder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .anonymous(postRequestDto.isAnonymous())
                .images(postRequestDto.getImages())
                .build();

        Post savedPost = postService.updatePost(updatedPost);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        boolean isScrapped = postScrapFilterService.isUserScrappedPost(userId, postId);
        boolean isLiked = postLikeFilterService.isUserLikedPost(userId, postId);
        boolean isAuthor = savedPost.getUser().getId().equals(userId);

        return ResponseEntity.ok(
                postMapper.toPostResponseDto(savedPost, userService, blockedUserIdList, isScrapped, isLiked, isAuthor, userId)
        );
    }
    // 게시글 삭제 api
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    // 전체 게시글 조회 api
    @GetMapping("/all")
    public ResponseEntity<PostListResponseDto> getAllPosts(Pageable pageable, @AuthUserId String userId) {
        Page<Post> postPage = postService.getAllPosts(pageable);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        List<PostListResponseDto.PostResponseDto> postResponseDtoList = postPage.getContent().stream()
                .map(post -> postMapper.toPostResponseDto(
                        post, userService, blockedUserIdList,
                        postScrapFilterService.isUserScrappedPost(userId, post.getId()),
                        postLikeFilterService.isUserLikedPost(userId, post.getId()),
                        post.getUser().getId().equals(userId), userId))
                .filter(postResponseDto -> postResponseDto.getIsBlocked() == null || !postResponseDto.getIsBlocked())
                .toList();

        return ResponseEntity.ok(new PostListResponseDto(postResponseDtoList, PagingResponse.from(postPage)));
    }

    // 카테고리별 게시글 조회 api
    @GetMapping("/category")
    public ResponseEntity<PostListResponseDto> getPostsByCategory(
            @RequestParam("category") Category category, Pageable pageable, @AuthUserId String userId) {

        Page<Post> postPage = postService.listByCategory(category, pageable);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        List<PostListResponseDto.PostResponseDto> postResponseDtoList = postPage.getContent().stream()
                .map(post -> postMapper.toPostResponseDto(
                        post, userService, blockedUserIdList,
                        postScrapFilterService.isUserScrappedPost(userId, post.getId()),
                        postLikeFilterService.isUserLikedPost(userId, post.getId()),
                        post.getUser().getId().equals(userId), userId))
                .filter(postResponseDto -> postResponseDto.getIsBlocked() == null || !postResponseDto.getIsBlocked())
                .toList();

        return ResponseEntity.ok(new PostListResponseDto(postResponseDtoList, PagingResponse.from(postPage)));
    }

    // 내가 작성한 게시글 조회 api
    @GetMapping("/my-posts")
    public ResponseEntity<PostListResponseDto> getUserPostsByCategory(
            @AuthUserId String userId, @RequestParam Category category, Pageable pageable, @RequestParam SortOption sortOption) {

        Page<Post> userPosts = postService.getUserPostsByCategory(userId, category, pageable, sortOption);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        List<PostListResponseDto.PostResponseDto> postResponseDtoList = userPosts.getContent().stream()
                .map(post -> postMapper.toPostResponseDto(post, userService, blockedUserIdList, false, false, true, userId))
                .toList();

        return ResponseEntity.ok(new PostListResponseDto(postResponseDtoList, PagingResponse.from(userPosts)));
    }

    // 내가 스크랩한 게시글 조회 api
    @GetMapping("/scraps")
    public ResponseEntity<PostScrapFilterResponseDto> getScrapsByUserAndCategory(
            @AuthUserId String userId,
            @RequestParam Category category,
            Pageable pageable,
            @RequestParam(required = false, defaultValue = "NEWEST_FIRST") SortOption sortOption) {
        Page<PostScrapFilter> scraps = postScrapFilterService.getScrapsByUserAndCategory(userId, category, pageable, sortOption);
        List<PostScrapFilterResponseDto.PostScrapFilterListResponseDto> postScrapFilterResponseDtoList = scraps.getContent().stream()
                .map(scrap -> postScrapFilterMapper.toListResponseDto(scrap, postScrapFilterService))
                .toList();

        PagingResponse pagingResponse = PagingResponse.from(scraps);

        PostScrapFilterResponseDto response = PostScrapFilterResponseDto.builder()
                .postScrapList(postScrapFilterResponseDtoList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    // 게시글 스크랩 토글 api
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<?> toggleScrap(@PathVariable Long postId, @AuthUserId String userId) {
        postScrapFilterService.toggleScrap(postId, userId);
        return ResponseEntity.ok().build();
    }

    // 게시글 좋아요 토글 api
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(@AuthUserId String userId, @PathVariable Long postId) {
        postLikeFilterService.toggleLike(userId, postId);
        return ResponseEntity.ok().build();
    }

    // 내가 좋아요한 게시글 조회 api
    @GetMapping("/liked")
    public ResponseEntity<PostListResponseDto> getLikedPostsByUser(@AuthUserId String userId, Pageable pageable) {
        Page<Post> likedPosts = postLikeFilterService.getLikedPostsByUser(userId, pageable);
        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        List<PostListResponseDto.PostResponseDto> postResponseDtoList = likedPosts.getContent().stream()
                .map(post -> postMapper.toPostResponseDto(post, userService, blockedUserIdList, false, true, post.getUser().getId().equals(userId), userId))
                .toList();

        return ResponseEntity.ok(new PostListResponseDto(postResponseDtoList, PagingResponse.from(likedPosts)));
    }

    // 메인홈 - 실시간 랭킹
    @GetMapping("/main/top")
    public ResponseEntity<PostRankingResponseDto.PagedPostRankingResponseDto> getTopPosts(@AuthUserId String userId, Pageable pageable) {
        Page<Post> postPage = postService.getTopPosts(pageable);

        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        List<PostRankingResponseDto> postList = postPage.getContent().stream()
                .map(post -> postMapper.toPostRankingResponseDto(
                        post, blockedUserIdList
                ))
                .toList();

        PagingResponse pagingResponse = PagingResponse.from(postPage);

        PostRankingResponseDto.PagedPostRankingResponseDto response = PostRankingResponseDto.PagedPostRankingResponseDto.builder()
                .category(null)
                .postList(postList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    // 메인홈 - 카테고리별 실시간 랭킹
    @GetMapping("/main/top/category")
    public ResponseEntity<PostRankingResponseDto.PagedPostRankingResponseDto> getTopPostsByCategory(
            @AuthUserId String userId,
            @RequestParam("category") Category category,
            Pageable pageable) {
        Page<Post> postPage = postService.getTopPostsByCategory(category, pageable);

        List<String> blockedUserIdList = userService.getBlockedUserIdList(userId);

        List<PostRankingResponseDto> postList = postPage.getContent().stream()
                .map(post -> postMapper.toPostRankingResponseDto(
                        post, blockedUserIdList
                ))
                .toList();

        PagingResponse pagingResponse = PagingResponse.from(postPage);

        PostRankingResponseDto.PagedPostRankingResponseDto response = PostRankingResponseDto.PagedPostRankingResponseDto.builder()
                .category(category.name())
                .postList(postList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    // 게시글 검색 api
    @GetMapping("/search")
    public ResponseEntity<PostSearchResponseDto> searchPostsByTitle(
            @RequestParam String title,
            @AuthUserId String userId,
            Pageable pageable) {
        Page<Post> posts = postService.searchPostsByTitle(title, userId, pageable);
        List<PostSearchResponseDto.PostSearchListResponseDto> postResponseDtoList = posts.getContent().stream()
                .map(postMapper::toPostSearchListResponseDto)
                .toList();

        PagingResponse pagingResponse = PagingResponse.from(posts);

        PostSearchResponseDto response = PostSearchResponseDto.builder()
                .data(postResponseDtoList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    // 최근 검색어 조회 API
    @GetMapping("/recent-searches")
    public ResponseEntity<List<String>> getRecentSearchTerms(@AuthUserId String userId) {
        List<String> recentSearchTerms = postSearchHistoryService.getRecentSearchTerms(userId);
        return ResponseEntity.ok(recentSearchTerms);
    }

    // 특정 검색어 삭제 API
    @DeleteMapping("/recent-searches/{id}")
    public ResponseEntity<Void> deleteSearchTermById(@PathVariable Long id) {
        postSearchHistoryService.deleteSearchTermById(id);
        return ResponseEntity.noContent().build();
    }

    // 게시글 신고 API
    @PostMapping("/{postId}/report")
    public ResponseEntity<Void> reportPost(@PathVariable Long postId,
                                           @RequestBody ReportTypeDto reportTypeDto){
        postReportService.postReport(postId, reportTypeDto.getReportType());
        return ResponseEntity.ok().build();
    }

    // 신고 당한 게시글 내역 조회 API
    @GetMapping("/report")
    public ResponseEntity<PostReportListResponseDto> getAllReportPosts(@AuthUserId String userId) {

        // 일단 신고당한 게시물들을 중복 없이 가져오기
        List<Post> reportedPostList = postReportService.findReportedPostList();
        List<ReportPostData> reportedPostDataList = new ArrayList<>();

        for (Post post : reportedPostList){
            HashMap<Report, Integer> reportTypeCounts = postReportService.getReportTypeCounts(post);
            reportedPostDataList.add(
                    ReportPostData.builder()
                            .id(post.getId())
                            .userId(post.getUser().getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .anonymous(post.getAnonymous())
                            .images(post.getImages())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .scrapCount(post.getScrapCount())
                            .viewCount(post.getViewCount())
                            .category(post.getCategory())
                            .comments(post.getComments())
                            .nickname(post.getNickname())
                            .profileImage(post.getProfileImage())
                            .email(post.getUser().getEmail())
                            .reportTypeCounts(reportTypeCounts)
                            .createdAt(post.getCreatedAt())
                            .lastModifiedAt(post.getLastModifiedAt())
                            .build());
        }

        List<PostReportListResponseDto.PostReportResponseDto> postReportResponseDtoList = reportedPostDataList.stream()
                .map(reportedPost -> postMapper.toReportedPostResponseDto(reportedPost))
                .toList();

        PostReportListResponseDto response = PostReportListResponseDto.builder()
                .postReportList(postReportResponseDtoList)
                .build();

        return ResponseEntity.ok(response);
    }

    // 신고 처리 API
    @PostMapping("/handleReport")
    public ResponseEntity<Void> handleReport(@RequestBody String email,
                                             @RequestBody Long postId,
                                             @RequestBody HandleReport handleReportType){
        postReportService.handleReport(email, postId, handleReportType);
        return ResponseEntity.ok().build();
    }
}

package com.project.foradhd.domain.board.business.service.Impl;

import static com.project.foradhd.global.exception.ErrorCode.NOT_FOUND_POST;
import static com.project.foradhd.global.exception.ErrorCode.NOT_FOUND_USER;

import com.project.foradhd.domain.board.business.service.PostReportService;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.entity.ReportPost;
import com.project.foradhd.domain.board.persistence.enums.HandleReport;
import com.project.foradhd.domain.board.persistence.enums.Report;
import com.project.foradhd.domain.board.persistence.repository.PostRepository;
import com.project.foradhd.domain.board.persistence.repository.ReportPostRepository;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.repository.UserRepository;
import com.project.foradhd.global.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostReportServiceImpl implements PostReportService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportPostRepository reportPostRepository;

    @Override
    @Transactional
    public void postReport(Long postId, Report reportType){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_POST));

        ReportPost reportPost = reportPostRepository.findByPostAndReportType(post, reportType)
                .orElse(null);

        if (reportPost != null){
            // 기존 신고 유형이 있으면 count 증가
            reportPost.increaseCount();
        } else {
            // 없으면 새롭게 신고 생성
            reportPost = ReportPost.builder()
                    .post(post)
                    .reportType(reportType)
                    .build();
            reportPostRepository.save(reportPost);
        }
    }

    @Override
    @Transactional
    public void handleReport(Long postId, HandleReport handleReportType){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_POST));

        User user = post.getUser();

        user.updateUserStatus(handleReportType);

        userRepository.save(user);

        reportPostRepository.deleteByPost(post);
        postRepository.delete(post);
    }

    // 2일 뒤 자동으로 상태를 CLEAN으로 변경
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 매일 한 번씩 실행
    public void resetUserStatusToClean() {
        LocalDateTime now = LocalDateTime.now();

        // 2일 정지된 유저만 검색해서 가져오기
        List<User> users = userRepository.findByStatus(HandleReport.DAY_2_PAUSE);

        for (User user : users) {
            if (user.getLastModifiedAt().plusDays(2).isBefore(now)) {
                user.updateUserStatus(HandleReport.CLEAN); // 2일이 지난 유저의 상태를 CLEAN으로 변경
                userRepository.save(user);
            }
        }
    }

    @Override
    public List<Post> findReportedPostList(){
        return reportPostRepository.findAllDistinctReportedPost();
    }

    @Override
    public HashMap<Report, Integer> getReportTypeCounts(Post post){
        HashMap<Report, Integer> reportTypeCounts = new HashMap<>();
        List<ReportPost> reportPostList = reportPostRepository.findByPost(post);

        for (ReportPost reportPost : reportPostList){
            reportTypeCounts.put(reportPost.getReportType(), reportPost.getReportCount());
        }

        return reportTypeCounts;
    }
}
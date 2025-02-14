package com.project.foradhd.domain.board.business.service.Impl;

import static com.project.foradhd.global.exception.ErrorCode.NOT_FOUND_POST;
import static com.project.foradhd.global.exception.ErrorCode.NOT_FOUND_USER;

import com.project.foradhd.domain.board.business.service.PostReportService;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.entity.ReportPost;
import com.project.foradhd.domain.board.persistence.enums.Report;
import com.project.foradhd.domain.board.persistence.repository.PostRepository;
import com.project.foradhd.domain.board.persistence.repository.ReportPostRepository;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.repository.UserRepository;
import com.project.foradhd.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostReportServiceImpl implements PostReportService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportPostRepository reportPostRepository;
    private final PostReportService postReportService;

    @Override
    @Transactional
    public void postReport(Long postId, Report reportType){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_POST));

        User user = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_USER));

        ReportPost reportPost = ReportPost.builder()
                .user(user)
                .post(post)
                .reportType(reportType)
                .build();

        // 레포지토리에 저장
        reportPostRepository.save(reportPost);

    }
}
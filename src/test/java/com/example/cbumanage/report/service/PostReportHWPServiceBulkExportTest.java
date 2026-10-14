package com.example.cbumanage.report.service;

import com.amazonaws.services.s3.AmazonS3;
import com.example.cbumanage.global.setting.service.SystemSettingService;
import com.example.cbumanage.group.repository.GroupRepository;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.report.entity.PostReport;
import com.example.cbumanage.report.repository.PostReportRepository;
import com.example.cbumanage.reportmember.repository.ReportMemberRepository;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostReportHWPServiceBulkExportTest {

    @Mock PostRepository postRepository;
    @Mock PostReportRepository postReportRepository;
    @Mock ReportMemberRepository reportMemberRepository;
    @Mock UserRepository userRepository;
    @Mock GroupRepository groupRepository;
    @Mock AmazonS3 amazonS3;
    @Mock SystemSettingService systemSettingService;
    @Mock PostReportService postReportService;

    @InjectMocks PostReportHWPService service;

    private static final long ADMIN_ID = 1L;
    private static final byte[] OLE_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0};

    @BeforeEach
    void admin() {
        User admin = user(ADMIN_ID, "관리자", Role.ROLE_ADMIN);
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    }

    @Test
    void 상한을_넘으면_만들지_않고_건수를_알려준다() {
        stubList(previewPage(List.of(preview(1L, "QA스터디", "관리자", date(5))), PostReportHWPService.MAX_BULK_EXPORT + 1));

        PostReportHWPService.TooManyReportsException e = assertThrows(
                PostReportHWPService.TooManyReportsException.class,
                () -> service.exportFilteredToZip(ADMIN_ID, null, null, null, null, null));
        assertEquals(PostReportHWPService.MAX_BULK_EXPORT + 1, e.getCount());
    }

    @Test
    void 조건에_맞는_보고서가_없으면_404_경로로_빠진다() {
        stubList(previewPage(List.of(), 0));

        assertThrows(EntityNotFoundException.class,
                () -> service.exportFilteredToZip(ADMIN_ID, null, null, null, null, null));
    }

    @Test
    void 필터에_걸린_보고서를_팀_작성자_날짜_이름으로_묶고_겹치면_번호를_붙인다() throws Exception {
        // 같은 팀·작성자·날짜 2건 + 다른 팀 1건
        List<PostDTO.PostReportPreviewDTO> previews = List.of(
                preview(10L, "QA스터디", "관리자", date(5)),
                preview(11L, "QA스터디", "관리자", date(5)),
                preview(12L, "알고리즘/스터디", "홍길동", date(6)));
        stubList(previewPage(previews, previews.size()));
        for (PostDTO.PostReportPreviewDTO p : previews) stubReport(p);
        when(systemSettingService.getValue(anyString(), anyString())).thenReturn("");

        PostReportHWPService.ZipExportResult result =
                service.exportFilteredToZip(ADMIN_ID, null, null, null, null, null);

        assertTrue(result.fileName().startsWith("보고서_일괄_"), result.fileName());
        List<String> names = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(result.zipBytes()), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
                byte[] head = zis.readNBytes(4);
                assertArrayEquals(OLE_MAGIC, head, entry.getName() + " 은 HWP(OLE) 파일이어야 한다");
            }
        }
        assertEquals(List.of(
                "[QA스터디]_관리자_261005.hwp",
                "[QA스터디]_관리자_261005_1.hwp",
                "[알고리즘_스터디]_홍길동_261006.hwp"), names);
    }

    // ---- fixtures ----

    private void stubList(Page<PostDTO.PostReportPreviewDTO> page) {
        when(postReportService.getPostReportPreviewDTOList(any(Pageable.class), eq(ADMIN_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new PostDTO.PostReportPreviewSearchDTO(page, PostDTO.ReportSearchInfoDTO.none()));
    }

    private void stubReport(PostDTO.PostReportPreviewDTO p) {
        Post post = new Post(100L, "제목 " + p.postId(), "내용", 7);
        ReflectionTestUtils.setField(post, "id", p.postId());
        PostReport report = new PostReport(post, p.groupId(), p.date(), "동아리방", null, "느낀 점", "다음 계획");
        ReflectionTestUtils.setField(report, "id", p.postId() + 1000);
        when(postRepository.findById(p.postId())).thenReturn(Optional.of(post));
        when(postReportRepository.findByPostId(p.postId())).thenReturn(Optional.of(report));
        when(reportMemberRepository.findByReportId(p.postId() + 1000)).thenReturn(List.of());
        when(userRepository.findById(100L)).thenReturn(Optional.of(user(100L, p.authorName(), Role.ROLE_USER)));
    }

    private static Page<PostDTO.PostReportPreviewDTO> previewPage(List<PostDTO.PostReportPreviewDTO> content, long total) {
        return new PageImpl<>(content, PageRequest.of(0, 200), total);
    }

    private static PostDTO.PostReportPreviewDTO preview(long postId, String group, String author, LocalDateTime date) {
        return new PostDTO.PostReportPreviewDTO(postId, "제목 " + postId, date, 100L, author, 1L, 282L, group, 2L, 1, date);
    }

    private static LocalDateTime date(int day) {
        return LocalDateTime.of(2026, 10, day, 9, 0);
    }

    private static User user(long id, String name, Role role) {
        User u = new User("u" + id + "@tukorea.ac.kr", 2020000000L + id, "pw");
        ReflectionTestUtils.setField(u, "userId", id);
        ReflectionTestUtils.setField(u, "name", name);
        ReflectionTestUtils.setField(u, "role", role);
        return u;
    }
}

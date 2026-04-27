package com.example.cbumanage.report.service;

import com.amazonaws.services.s3.AmazonS3;
import com.example.cbumanage.global.util.ImageCompressUtil;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.report.entity.PostReport;
import com.example.cbumanage.report.repository.PostReportRepository;
import com.example.cbumanage.reportmember.dto.ReportMemberDTO;
import com.example.cbumanage.reportmember.entity.ReportMember;
import com.example.cbumanage.reportmember.repository.ReportMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.gso.ControlPicture;
import kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControl;
import kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.ParagraphList;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.HWPChar;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.HWPCharNormal;
import kr.dogfoot.hwplib.object.docinfo.BinData;
import kr.dogfoot.hwplib.object.docinfo.bindata.BinDataCompress;
import kr.dogfoot.hwplib.object.docinfo.bindata.BinDataType;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.writer.HWPWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostReportHWPService {

    private final PostRepository postRepository;
    private final PostReportRepository postReportRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final ReportMemberRepository reportMemberRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;

    @Value("${aws_bucket}")
    private String awsBucket;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    public record HWPExportResult(String title, byte[] hwpBytes) {}

    /**
     * ADMIN / MANAGER 권한 확인 후 보고서 HWP 파일 생성
     */
    public HWPExportResult exportToHWP(Long postId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        if (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // 데이터 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostReport report = postReportRepository.findByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("Report Not Found"));
        CbuMember author = cbuMemberRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author Not Found"));

        // 참여 멤버 조회
        List<ReportMember> reportMembers = reportMemberRepository.findByReportId(report.getId());
        List<ReportMemberDTO.ReportMemberInfoDTO> members = reportMembers.stream()
                .map(rm -> {
                    CbuMember m = cbuMemberRepository.findById(rm.getMemberId())
                            .orElseThrow(() -> new EntityNotFoundException("Member Not Found: " + rm.getMemberId()));
                    return new ReportMemberDTO.ReportMemberInfoDTO(
                            m.getCbuMemberId(),
                            m.getName(),
                            m.getStudentNumber(),
                            m.getMajor()
                    );
                })
                .collect(Collectors.toList());

        // DTO 빌드
        PostDTO.PostReportToHWPDTO dto = new PostDTO.PostReportToHWPDTO(
                post.getTitle(),
                author.getName(),
                post.getContent(),
                report.getLocation(),
                report.getReportImage(),
                report.getDate(),
                report.getDate() != null ? String.valueOf(report.getDate().getMonthValue()) : "",
                members.size(),
                members
        );

        // S3에서 이미지 다운로드 & JPEG 압축 — generateHWP 호출 전 별도 처리
        byte[] imageBytes = null;
        if (dto.reportImage() != null && !dto.reportImage().isBlank()) {
            try {
                String s3Key = extractS3Key(dto.reportImage());
                try (InputStream s3Stream = amazonS3.getObject(awsBucket, s3Key).getObjectContent()) {
                    imageBytes = ImageCompressUtil.compressToJpeg(s3Stream, 1200, 900, 0.85f);
                }
            } catch (Exception ignored) {
                // 이미지 다운로드/압축 실패 시 이미지 없이 생성
            }
        }

        byte[] hwpBytes = generateHWP(dto, imageBytes);
        return new HWPExportResult(dto.title(), hwpBytes);
    }

    // -----------------------------------------------------------------------
    // S3 키 추출
    // -----------------------------------------------------------------------

    private String extractS3Key(String imageUrl) {
        int idx = imageUrl.indexOf("uploads/");
        if (idx < 0) throw new IllegalArgumentException("S3 key를 추출할 수 없습니다: " + imageUrl);
        return imageUrl.substring(idx);
    }

    // -----------------------------------------------------------------------
    // HWP 생성
    // -----------------------------------------------------------------------

    private byte[] generateHWP(PostDTO.PostReportToHWPDTO dto, byte[] imageBytes) throws Exception {
        InputStream is = getClass().getResourceAsStream("/templates/HWPTemplate.hwp");
        if (is == null) throw new IllegalStateException("HWP 템플릿 파일을 찾을 수 없습니다.");
        HWPFile hwpFile = HWPReader.fromInputStream(is);

        // 텍스트 플레이스홀더 치환
        Map<String, String> placeholders = buildPlaceholders(dto);
        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                processParagraph(para, placeholders);
            }
        }

        // 표 안 이미지 교체 — 미리 준비된 imageBytes 사용
        if (imageBytes != null && imageBytes.length > 0) {
            int oldBinItemId = findPictureInsideTableOnly(hwpFile);
            if (oldBinItemId >= 0) {
                replaceImageByBinItemId(hwpFile, oldBinItemId, imageBytes);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HWPWriter.toStream(hwpFile, out);
        return out.toByteArray();
    }

    // -----------------------------------------------------------------------
    // 플레이스홀더 맵 구성
    // -----------------------------------------------------------------------

    private Map<String, String> buildPlaceholders(PostDTO.PostReportToHWPDTO dto) {
        Map<String, String> map = new HashMap<>();
        map.put("{authorName}", dto.authorName() != null ? dto.authorName() : "");
        map.put("{month}", dto.month() != null ? dto.month() : "");
        map.put("{date}", dto.date() != null ? dto.date().format(DATE_FORMATTER) : "");
        map.put("{location}", dto.location() != null ? dto.location() : "");
        map.put("{content}", dto.content() != null ? dto.content() : "");
        map.put("{membercount}", String.valueOf(dto.memberCount()));

        List<ReportMemberDTO.ReportMemberInfoDTO> members = dto.reportMembers();
        for (int i = 0; i < 30; i++) {
            String suffix = String.valueOf(i + 1);
            if (members != null && i < members.size()) {
                ReportMemberDTO.ReportMemberInfoDTO m = members.get(i);
                map.put("{name" + suffix + "}", m.name() != null ? m.name() : "");
                map.put("{dep" + suffix + "}", m.major() != null ? m.major() : "");
                map.put("{num" + suffix + "}", m.studentNumber() != null ? String.valueOf(m.studentNumber()) : "");
            } else {
                map.put("{name" + suffix + "}", "");
                map.put("{dep" + suffix + "}", "");
                map.put("{num" + suffix + "}", "");
            }
        }
        return map;
    }

    // -----------------------------------------------------------------------
    // 텍스트 치환
    // -----------------------------------------------------------------------

    private void processParagraph(Paragraph para, Map<String, String> placeholders) {
        if (para.getText() != null) {
            replaceInCharList(para.getText().getCharList(), placeholders);
        }

        ArrayList<Control> controls = para.getControlList();
        if (controls == null) return;

        for (Control control : controls) {
            if (control instanceof ControlTable tableControl) {
                for (Row row : tableControl.getRowList()) {
                    for (Cell cell : row.getCellList()) {
                        ParagraphList pl = cell.getParagraphList();
                        for (int i = 0; i < pl.getParagraphCount(); i++) {
                            processParagraph(pl.getParagraph(i), placeholders);
                        }
                    }
                }
            }
        }
    }

    private void replaceInCharList(ArrayList<HWPChar> charList, Map<String, String> placeholders) {
        List<Integer> normalIndices = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < charList.size(); i++) {
            HWPChar c = charList.get(i);
            if (c instanceof HWPCharNormal && c.getCode() != 13) {
                normalIndices.add(i);
                sb.appendCodePoint(c.getCode());
            }
        }

        String text = sb.toString();
        boolean changed = false;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
                changed = true;
            }
        }
        if (!changed || normalIndices.isEmpty()) return;

        // 역순 삭제 (인덱스 보존)
        for (int i = normalIndices.size() - 1; i >= 0; i--) {
            charList.remove((int) normalIndices.get(i));
        }

        // 첫 위치에 새 문자 삽입
        int insertPos = Math.min(normalIndices.get(0), charList.size());
        int[] codePoints = text.codePoints().toArray();
        for (int i = 0; i < codePoints.length; i++) {
            charList.add(insertPos + i, new HWPCharNormal(codePoints[i]));
        }
    }

    // -----------------------------------------------------------------------
    // 이미지 교체
    // -----------------------------------------------------------------------

    private int findPictureInsideTableOnly(HWPFile hwpFile) {
        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                ArrayList<Control> controls = para.getControlList();
                if (controls == null) continue;
                for (Control ctrl : controls) {
                    if (!(ctrl instanceof ControlTable tableControl)) continue;
                    int id = findPictureInCells(tableControl);
                    if (id >= 0) return id;
                }
            }
        }
        return -1;
    }

    private int findPictureInCells(ControlTable tableControl) {
        for (Row row : tableControl.getRowList()) {
            for (Cell cell : row.getCellList()) {
                ParagraphList pl = cell.getParagraphList();
                for (int i = 0; i < pl.getParagraphCount(); i++) {
                    ArrayList<Control> controls = pl.getParagraph(i).getControlList();
                    if (controls == null) continue;
                    for (Control ctrl : controls) {
                        if (ctrl instanceof GsoControl gso
                                && gso.getGsoType() == GsoControlType.Picture) {
                            try {
                                return ((ControlPicture) gso)
                                        .getShapeComponentPicture()
                                        .getPictureInfo()
                                        .getBinItemID();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        return -1;
    }

    private void replaceImageByBinItemId(HWPFile hwpFile, int oldBinItemId, byte[] imageBytes) {
        try {
            // 새 ID 결정 (현재 최대 ID + 1)
            int newId = hwpFile.getDocInfo().getBinDataList().stream()
                    .mapToInt(BinData::getBinDataID)
                    .max().orElse(0) + 1;

            // DocInfo에 메타 등록
            BinData newMeta = hwpFile.getDocInfo().addNewBinData();
            newMeta.setBinDataID(newId);
            newMeta.setExtensionForEmbedding("jpg");
            newMeta.getProperty().setType(BinDataType.Embedding);
            newMeta.getProperty().setCompress(BinDataCompress.NoCompress); // JPEG는 이미 압축됨

            // OLE 스트림에 바이너리 저장 — 확장자 포함 필수
            String newName = String.format("BIN%04d.jpg", newId);
            hwpFile.getBinData().addNewEmbeddedBinaryData(newName, imageBytes, BinDataCompress.NoCompress);

            // 표 안 ControlPicture의 BinItemID만 새 ID로 교체
            updatePictureBinItemId(hwpFile, oldBinItemId, newId);
        } catch (Exception ignored) {}
    }

    private void updatePictureBinItemId(HWPFile hwpFile, int oldId, int newId) {
        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                ArrayList<Control> controls = para.getControlList();
                if (controls == null) continue;
                for (Control ctrl : controls) {
                    if (!(ctrl instanceof ControlTable tableControl)) continue;
                    if (updateInTable(tableControl, oldId, newId)) return;
                }
            }
        }
    }

    private boolean updateInTable(ControlTable tableControl, int oldId, int newId) {
        for (Row row : tableControl.getRowList()) {
            for (Cell cell : row.getCellList()) {
                ParagraphList pl = cell.getParagraphList();
                for (int i = 0; i < pl.getParagraphCount(); i++) {
                    ArrayList<Control> controls = pl.getParagraph(i).getControlList();
                    if (controls == null) continue;
                    for (Control ctrl : controls) {
                        if (ctrl instanceof GsoControl gso
                                && gso.getGsoType() == GsoControlType.Picture) {
                            try {
                                var picInfo = ((ControlPicture) gso)
                                        .getShapeComponentPicture().getPictureInfo();
                                if (picInfo.getBinItemID() == oldId) {
                                    picInfo.setBinItemID(newId);
                                    return true;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        return false;
    }
}

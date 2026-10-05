package tools;

import com.example.cbumanage.global.util.ImageCompressUtil;
import com.example.cbumanage.report.service.HwpPictureFrame;
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
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 서버 없이 템플릿 + 사진 + 서명으로 활동 내역서 예시를 뽑는 로컬 도구.
 * PostReportHWPService 의 그림 교체·틀 맞춤 로직을 그대로 적용한다 (텍스트 치환은 대표자 이름만).
 *
 *   GENERATE_SAMPLE=true SAMPLE_PHOTO=/path/photo.jpg SAMPLE_SIGN=/path/sign.png SAMPLE_OUT=/path/out.hwp \
 *   ./gradlew test --tests tools.GenerateSampleHwpTest --rerun-tasks
 */
class GenerateSampleHwpTest {

    @Test
    void generate() throws Exception {
        if (!"true".equals(System.getenv("GENERATE_SAMPLE"))) return;

        Path photo = Path.of(System.getenv("SAMPLE_PHOTO"));
        Path sign = Path.of(System.getenv("SAMPLE_SIGN"));
        Path out = Path.of(System.getenv("SAMPLE_OUT"));

        HWPFile hwp;
        try (InputStream is = getClass().getResourceAsStream("/templates/HWPTemplate.hwp")) {
            hwp = HWPReader.fromInputStream(is);
        }

        // 대표자 이름
        for (Section section : hwp.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                if (para.getText() != null) replace(para.getText().getCharList(), "{president}", "황건하");
            }
        }

        // 활동 사진: 서비스와 같은 1차 축소(1200×900) → 틀을 사진 비율로 셀에 맞춤 → 교체
        byte[] photoBytes;
        try (InputStream in = Files.newInputStream(photo)) {
            photoBytes = ImageCompressUtil.compressToJpeg(in, 1200, 900, 0.85f);
        }
        HwpPictureFrame.Slot slot = HwpPictureFrame.findInTable(hwp);
        if (slot == null) throw new IllegalStateException("표 안 사진 틀을 못 찾음");
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(photoBytes));
        long beforeW = slot.picture().getHeader().getWidth(), beforeH = slot.picture().getHeader().getHeight();
        HwpPictureFrame.fitToCell(slot, img.getWidth(), img.getHeight());
        System.out.println("사진 " + img.getWidth() + "x" + img.getHeight() + " / 틀 " + beforeW + "x" + beforeH
                + " → " + slot.picture().getHeader().getWidth() + "x" + slot.picture().getHeader().getHeight());
        replaceImage(hwp, slot.binItemId(), photoBytes);

        // 서명: 서비스와 같은 600×400 축소
        byte[] signBytes;
        try (InputStream in = Files.newInputStream(sign)) {
            signBytes = ImageCompressUtil.compressToJpeg(in, 600, 400, 0.9f);
        }
        int signId = findPictureOutsideTable(hwp);
        if (signId < 0) throw new IllegalStateException("표 밖 서명 틀을 못 찾음");
        replaceImage(hwp, signId, signBytes);

        HWPWriter.toFile(hwp, out.toString());
        System.out.println("생성: " + out);
    }

    // ---- 아래는 PostReportHWPService 와 같은 로직 ----

    private int findPictureOutsideTable(HWPFile hwpFile) {
        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                ArrayList<Control> controls = para.getControlList();
                if (controls == null) continue;
                for (Control ctrl : controls) {
                    if (ctrl instanceof GsoControl gso && gso.getGsoType() == GsoControlType.Picture
                            && gso instanceof ControlPicture p) {
                        return p.getShapeComponentPicture().getPictureInfo().getBinItemID();
                    }
                }
            }
        }
        return -1;
    }

    private void replaceImage(HWPFile hwpFile, int oldId, byte[] bytes) throws Exception {
        int newId = hwpFile.getDocInfo().getBinDataList().stream().mapToInt(BinData::getBinDataID).max().orElse(0) + 1;
        BinData meta = hwpFile.getDocInfo().addNewBinData();
        meta.setBinDataID(newId);
        meta.setExtensionForEmbedding("jpg");
        meta.getProperty().setType(BinDataType.Embedding);
        meta.getProperty().setCompress(BinDataCompress.NoCompress);
        hwpFile.getBinData().addNewEmbeddedBinaryData(String.format("BIN%04d.jpg", newId), bytes, BinDataCompress.NoCompress);

        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                ArrayList<Control> controls = para.getControlList();
                if (controls == null) continue;
                for (Control ctrl : controls) {
                    if (ctrl instanceof ControlTable table) {
                        for (Row row : table.getRowList()) for (Cell cell : row.getCellList()) {
                            ParagraphList pl = cell.getParagraphList();
                            for (int i = 0; i < pl.getParagraphCount(); i++) {
                                ArrayList<Control> cs = pl.getParagraph(i).getControlList();
                                if (cs == null) continue;
                                for (Control c : cs) if (update(c, oldId, newId)) return;
                            }
                        }
                    } else if (update(ctrl, oldId, newId)) {
                        return;
                    }
                }
            }
        }
    }

    private boolean update(Control c, int oldId, int newId) {
        if (!(c instanceof GsoControl gso) || gso.getGsoType() != GsoControlType.Picture) return false;
        var info = ((ControlPicture) gso).getShapeComponentPicture().getPictureInfo();
        if (info.getBinItemID() == oldId) { info.setBinItemID(newId); return true; }
        return false;
    }

    private boolean replace(ArrayList<HWPChar> charList, String from, String to) {
        List<Integer> idx = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charList.size(); i++) {
            HWPChar ch = charList.get(i);
            if (ch instanceof HWPCharNormal && ch.getCode() != 13) { idx.add(i); sb.appendCodePoint(ch.getCode()); }
        }
        String text = sb.toString();
        if (!text.contains(from) || idx.isEmpty()) return false;
        text = text.replace(from, to);
        for (int i = idx.size() - 1; i >= 0; i--) charList.remove((int) idx.get(i));
        int at = Math.min(idx.get(0), charList.size());
        int[] cps = text.codePoints().toArray();
        for (int i = 0; i < cps.length; i++) charList.add(at + i, new HWPCharNormal(cps[i]));
        return true;
    }
}

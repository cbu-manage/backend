package tools;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.HWPChar;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.HWPCharNormal;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.writer.HWPWriter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 템플릿의 대표자 이름을 치환 자리표시자로 바꾸는 일회성 도구.
 * HWP 는 문단 레코드에 글자 수가 들어 있어 바이트 치환으로는 길이를 못 바꾼다.
 * 앱이 쓰는 hwplib 으로 열고 고쳐서 다시 쓴다.
 *
 *   PATCH_TEMPLATE=true ./gradlew test --tests tools.PatchTemplateTest --rerun-tasks
 */
class PatchTemplateTest {

    @Test
    void patch() throws Exception {
        if (!"true".equals(System.getenv("PATCH_TEMPLATE"))) return;

        Path path = Path.of("src/main/resources/templates/HWPTemplate.hwp");
        HWPFile file = HWPReader.fromFile(path.toString());

        int changed = 0;
        for (Section section : file.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                if (para.getText() == null) continue;
                if (replace(para.getText().getCharList(), "박채연", "{president}")) changed++;
            }
        }
        System.out.println("치환한 문단 수: " + changed);
        if (changed > 0) {
            HWPWriter.toFile(file, path.toString());
            System.out.println("템플릿 저장 완료");
        }
    }

    /** PostReportHWPService.replaceInCharList 와 같은 방식 */
    private boolean replace(ArrayList<HWPChar> charList, String from, String to) {
        List<Integer> idx = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charList.size(); i++) {
            HWPChar c = charList.get(i);
            if (c instanceof HWPCharNormal && c.getCode() != 13) {
                idx.add(i);
                sb.appendCodePoint(c.getCode());
            }
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

package com.example.cbumanage.report.service;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.control.gso.shapecomponent.ShapeComponent;
import kr.dogfoot.hwplib.object.bodytext.control.gso.shapecomponenteach.polygon.PositionXY;
import kr.dogfoot.hwplib.reader.HWPReader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HwpPictureFrameTest {

    private HWPFile template() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/templates/HWPTemplate.hwp")) {
            return HWPReader.fromInputStream(is);
        }
    }

    @Test
    void 가로_사진은_셀_높이에_맞춰_가로로_넓은_틀이_된다() throws Exception {
        HWPFile hwp = template();
        HwpPictureFrame.Slot slot = HwpPictureFrame.findInTable(hwp);
        assertNotNull(slot);

        long boxW = slot.cell().getListHeader().getWidth()
                - slot.cell().getListHeader().getLeftMargin() - slot.cell().getListHeader().getRightMargin();
        long boxH = slot.cell().getListHeader().getHeight()
                - slot.cell().getListHeader().getTopMargin() - slot.cell().getListHeader().getBottomMargin();

        HwpPictureFrame.fitToCell(slot, 1600, 900);

        long w = slot.picture().getHeader().getWidth();
        long h = slot.picture().getHeader().getHeight();
        assertEquals(boxH, h, "16:9 사진은 셀 높이가 한계");
        assertTrue(w <= boxW, "셀 너비를 넘지 않는다");
        assertEquals(16.0 / 9.0, (double) w / h, 0.01, "틀 비율 = 사진 비율");

        ShapeComponent sc = slot.picture().getShapeComponent();
        assertEquals(w, sc.getWidthAtCurrent());
        assertEquals(h, sc.getHeightAtCurrent());
        double sx = sc.getRenderingInfo().getScaleRotateMatrixPairList().get(0).getScaleMatrix().getValue(0);
        double sy = sc.getRenderingInfo().getScaleRotateMatrixPairList().get(0).getScaleMatrix().getValue(4);
        assertEquals((double) w / sc.getWidthAtCreate(), sx, 1e-9);
        assertEquals((double) h / sc.getHeightAtCreate(), sy, 1e-9);

        // 꼭짓점은 생성 시 좌표계 그대로 (변환행렬이 늘린다). 같이 키우면 두 번 확대된다.
        PositionXY rb = slot.picture().getShapeComponentPicture().getRightBottom();
        assertEquals(sc.getWidthAtCreate(), rb.getX());
        assertEquals(sc.getHeightAtCreate(), rb.getY());
    }

    @Test
    void 세로_사진은_셀_높이에_맞춰_좁은_틀이_된다() throws Exception {
        HWPFile hwp = template();
        HwpPictureFrame.Slot slot = HwpPictureFrame.findInTable(hwp);
        assertNotNull(slot);
        long boxH = slot.cell().getListHeader().getHeight()
                - slot.cell().getListHeader().getTopMargin() - slot.cell().getListHeader().getBottomMargin();

        HwpPictureFrame.fitToCell(slot, 900, 1600);

        long w = slot.picture().getHeader().getWidth();
        long h = slot.picture().getHeader().getHeight();
        assertEquals(boxH, h);
        assertEquals(9.0 / 16.0, (double) w / h, 0.01);
    }

    @Test
    void 크기를_모르면_틀을_건드리지_않는다() throws Exception {
        HWPFile hwp = template();
        HwpPictureFrame.Slot slot = HwpPictureFrame.findInTable(hwp);
        long before = slot.picture().getHeader().getWidth();

        HwpPictureFrame.fitToCell(slot, 0, 0);

        assertEquals(before, slot.picture().getHeader().getWidth());
    }
}

package com.example.cbumanage.report.service;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ctrlheader.CtrlHeaderGso;
import kr.dogfoot.hwplib.object.bodytext.control.gso.ControlPicture;
import kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControl;
import kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControlType;
import kr.dogfoot.hwplib.object.bodytext.control.gso.shapecomponent.ShapeComponent;
import kr.dogfoot.hwplib.object.bodytext.control.gso.shapecomponent.renderingnfo.Matrix;
import kr.dogfoot.hwplib.object.bodytext.control.gso.shapecomponent.renderingnfo.ScaleRotateMatrixPair;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.ListHeaderForCell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.ParagraphList;

import java.util.ArrayList;

/**
 * 활동 내역서 템플릿의 "표 안 그림 틀"(활동 사진 자리)을 다루는 도우미.
 *
 * 틀은 템플릿에 고정 크기로 박혀 있어 사진을 그대로 넣으면 틀 비율로 늘어난다.
 * {@link #fitToCell}은 틀을 사진 비율로, 셀 안쪽(여백 제외)에 들어가는 최대 크기로 다시 잡는다.
 */
public final class HwpPictureFrame {

    private HwpPictureFrame() {}

    /** 표 안 그림과 그 그림이 들어 있는 셀 */
    public record Slot(ControlPicture picture, Cell cell) {
        public int binItemId() {
            return picture.getShapeComponentPicture().getPictureInfo().getBinItemID();
        }
    }

    /** 표 안 첫 그림 = 활동 사진 자리. 없으면 null. */
    public static Slot findInTable(HWPFile hwpFile) {
        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph para : section) {
                ArrayList<Control> controls = para.getControlList();
                if (controls == null) continue;
                for (Control ctrl : controls) {
                    if (!(ctrl instanceof ControlTable table)) continue;
                    for (Row row : table.getRowList()) {
                        for (Cell cell : row.getCellList()) {
                            ParagraphList pl = cell.getParagraphList();
                            for (int i = 0; i < pl.getParagraphCount(); i++) {
                                ArrayList<Control> cs = pl.getParagraph(i).getControlList();
                                if (cs == null) continue;
                                for (Control c : cs) {
                                    if (c instanceof GsoControl gso
                                            && gso.getGsoType() == GsoControlType.Picture
                                            && gso instanceof ControlPicture picture) {
                                        return new Slot(picture, cell);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 틀을 사진 비율(imgW:imgH)로 셀 안쪽에 들어가는 최대 크기로 바꾼다.
     *
     * 바꾸는 것: 컨트롤 헤더 크기, 도형 현재 크기, 회전 중심, 크기 변환행렬의 배율(sx, sy).
     * 건드리지 않는 것: 그림 꼭짓점·이미지 크기 — 이들은 '생성 시 크기' 좌표계에 있고 변환행렬이
     * 현재 크기로 늘린다. 같이 키우면 두 번 확대돼 사진 일부만 보인다.
     * 틀이 "글자처럼 취급"이면 위치는 문단 정렬이 정하므로 오프셋도 두고, 아니면 셀 가운데로 맞춘다.
     */
    public static void fitToCell(Slot slot, int imgW, int imgH) {
        if (imgW <= 0 || imgH <= 0) return;
        ListHeaderForCell lh = slot.cell().getListHeader();
        long boxW = lh.getWidth() - lh.getLeftMargin() - lh.getRightMargin();
        long boxH = lh.getHeight() - lh.getTopMargin() - lh.getBottomMargin();
        if (boxW <= 0 || boxH <= 0) return;

        double ratio = Math.min((double) boxW / imgW, (double) boxH / imgH);
        long newW = Math.round(imgW * ratio);
        long newH = Math.round(imgH * ratio);

        CtrlHeaderGso header = slot.picture().getHeader();
        header.setWidth(newW);
        header.setHeight(newH);
        if (!header.getProperty().isLikeWord()) {
            header.setxOffset(Math.max(0, (boxW - newW) / 2));
            header.setyOffset(Math.max(0, (boxH - newH) / 2));
        }

        ShapeComponent sc = slot.picture().getShapeComponent();
        sc.setWidthAtCurrent((int) newW);
        sc.setHeightAtCurrent((int) newH);
        sc.setRotateXCenter((int) (newW / 2));
        sc.setRotateYCenter((int) (newH / 2));

        if (sc.getWidthAtCreate() > 0 && sc.getHeightAtCreate() > 0) {
            double sx = (double) newW / sc.getWidthAtCreate();
            double sy = (double) newH / sc.getHeightAtCreate();
            for (ScaleRotateMatrixPair pair : sc.getRenderingInfo().getScaleRotateMatrixPairList()) {
                Matrix m = pair.getScaleMatrix();   // 행 우선 2×3: [sx, 0, dx, 0, sy, dy]
                m.setValue(0, sx);
                m.setValue(4, sy);
            }
        }
    }
}

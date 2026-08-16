package com.mujin.document.util.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PdfBoxLayoutEngine 单元测试
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
class PdfBoxLayoutEngineTest {

    private PDDocument pdfDoc;
    private PdfBoxLayoutEngine layout;

    @BeforeEach
    void setUp() {
        pdfDoc = new PDDocument();
        layout = new PdfBoxLayoutEngine(pdfDoc);
    }

    @Test
    @DisplayName("默认配置：A4 + 20mm 边距")
    void testDefaultConfig() {
        assertThat(layout.getPageSize().getWidth()).isEqualTo(595f);
        assertThat(layout.getPageSize().getHeight()).isEqualTo(842f);
        // 顶部 Y 应为 高度 - 边距
        float expectedTop = 842f - 20f * 2.83465f;
        assertThat(layout.getCurrentY()).isEqualTo(expectedTop);
    }

    @Test
    @DisplayName("resolvePageSize：A4 / LETTER / 默认")
    void testResolvePageSize() {
        assertThat(PdfBoxLayoutEngine.resolvePageSize("A4").getWidth()).isEqualTo(595f);
        assertThat(PdfBoxLayoutEngine.resolvePageSize("LETTER").getWidth()).isEqualTo(612f);
        assertThat(PdfBoxLayoutEngine.resolvePageSize(null).getWidth()).isEqualTo(595f);
        assertThat(PdfBoxLayoutEngine.resolvePageSize("OTHER").getWidth()).isEqualTo(595f);
    }

    @Test
    @DisplayName("configure：自定义页面尺寸与边距")
    void testConfigure() {
        layout.configure("LETTER", 10f);
        assertThat(layout.getPageSize().getWidth()).isEqualTo(612f);
        assertThat(layout.getMarginX()).isEqualTo(10f * 2.83465f);
    }

    @Test
    @DisplayName("newPage：重置 Y 到顶部")
    void testNewPage() {
        layout.advanceY(100);
        layout.newPage();
        float expectedTop = layout.getPageSize().getHeight() - layout.getMarginX();
        assertThat(layout.getCurrentY()).isEqualTo(expectedTop);
        assertThat(layout.getCurrentPage()).isNotNull();
    }

    @Test
    @DisplayName("advanceY：Y 坐标递减")
    void testAdvanceY() {
        float before = layout.getCurrentY();
        layout.advanceY(30);
        assertThat(layout.getCurrentY()).isEqualTo(before - 30);
    }

    @Test
    @DisplayName("needsNewPage：低于边距返回 true")
    void testNeedsNewPage() {
        layout.setCurrentY(10);
        assertThat(layout.needsNewPage()).isTrue();
        layout.setCurrentY(100);
        assertThat(layout.needsNewPage()).isFalse();
    }

    @Test
    @DisplayName("getContentWidth：页面宽 - 2 × 边距")
    void testGetContentWidth() {
        layout.configure("A4", 20);
        float expected = 595f - 2 * (20f * 2.83465f);
        assertThat(layout.getContentWidth()).isEqualTo(expected);
    }
}

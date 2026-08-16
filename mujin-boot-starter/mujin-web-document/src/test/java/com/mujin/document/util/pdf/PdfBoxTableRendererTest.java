package com.mujin.document.util.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PdfBoxTableRenderer 单元测试
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
class PdfBoxTableRendererTest {

    private PdfBoxTableRenderer renderer;
    private PdfBoxFontRegistry fontReg;
    private PdfBoxLayoutEngine layout;
    private PDDocument pdfDoc;

    @BeforeEach
    void setUp() throws Exception {
        renderer = new PdfBoxTableRenderer();
        pdfDoc = new PDDocument();
        fontReg = new PdfBoxFontRegistry();
        fontReg.load(pdfDoc, new com.mujin.document.configuration.DocumentProperties());
        layout = new PdfBoxLayoutEngine(pdfDoc);
    }

    @Test
    @DisplayName("computeColumnWidths：按比例切分总宽")
    void testComputeColumnWidths() {
        float[] widths = {1.0f, 2.0f, 1.0f};
        float[] result = renderer.computeColumnWidths(widths, 400f);
        assertThat(result).containsExactly(100f, 200f, 100f);
    }

    @Test
    @DisplayName("computeColumnWidths：总和为 0 时按 0 处理")
    void testComputeColumnWidthsZeroRatio() {
        float[] widths = {0f, 0f};
        float[] result = renderer.computeColumnWidths(widths, 400f);
        // 全 0 比例时列宽均为 NaN/0，验证不会抛异常
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("truncate：长文本截断并加省略号")
    void testTruncateLong() {
        String result = PdfBoxTableRenderer.truncate("abcdefghijklmnopqrstuvwxyz", 10);
        assertThat(result).hasSize(10);
        assertThat(result).endsWith("…");
        assertThat(result.substring(0, 9)).isEqualTo("abcdefghi");
    }

    @Test
    @DisplayName("truncate：短文本不截断")
    void testTruncateShort() {
        assertThat(PdfBoxTableRenderer.truncate("abc", 10)).isEqualTo("abc");
        assertThat(PdfBoxTableRenderer.truncate(null, 10)).isEqualTo("");
    }

    @Test
    @DisplayName("truncate：恰好等于 maxLen 不截断")
    void testTruncateExact() {
        assertThat(PdfBoxTableRenderer.truncate("abcdefghij", 10)).isEqualTo("abcdefghij");
    }
}

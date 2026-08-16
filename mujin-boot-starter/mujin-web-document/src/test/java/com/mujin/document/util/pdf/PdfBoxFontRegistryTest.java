package com.mujin.document.util.pdf;

import com.mujin.document.configuration.DocumentProperties;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PdfBoxFontRegistry 单元测试
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
class PdfBoxFontRegistryTest {

    private PDDocument pdfDoc;
    private PdfBoxFontRegistry registry;

    @BeforeEach
    void setUp() {
        pdfDoc = new PDDocument();
        registry = new PdfBoxFontRegistry();
    }

    @Test
    @DisplayName("load：未配置 TTF 时降级到 Helvetica")
    void testLoadDefault() throws Exception {
        DocumentProperties props = new DocumentProperties();
        // 默认 fontPath 为空字符串
        registry.load(pdfDoc, props);

        assertThat(registry.getRegularFont()).isNotNull();
        assertThat(registry.getBoldFont()).isNotNull();
        assertThat(registry.getMonoFont()).isNotNull();
        assertThat(registry.isUsingTrueType()).isFalse();
    }

    @Test
    @DisplayName("load：fontPath 不存在时降级到 Helvetica")
    void testLoadFontPathNotExist() throws Exception {
        DocumentProperties props = new DocumentProperties();
        props.getPdfExport().setFontPath("Z:/non/existent/font.ttf");
        registry.load(pdfDoc, props);

        assertThat(registry.isUsingTrueType()).isFalse();
        assertThat(registry.getRegularFont()).isNotNull();
    }

    @Test
    @DisplayName("getRegularFont / getBoldFont / getMonoFont：返回非空字体")
    void testGetters() throws Exception {
        registry.load(pdfDoc, new DocumentProperties());
        PDFont regular = registry.getRegularFont();
        PDFont bold = registry.getBoldFont();
        PDFont mono = registry.getMonoFont();
        assertThat(regular).isNotNull();
        assertThat(bold).isNotNull();
        assertThat(mono).isNotNull();
    }

    @Test
    @DisplayName("load：fontPath 为 null 时不抛异常")
    void testLoadFontPathNull() throws Exception {
        DocumentProperties props = new DocumentProperties();
        props.getPdfExport().setFontPath(null);
        registry.load(pdfDoc, props);

        assertThat(registry.isUsingTrueType()).isFalse();
    }
}

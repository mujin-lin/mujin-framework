package com.mujin.document.util.pdf;

import com.mujin.document.configuration.DocumentProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;

/**
 * PDFBox 字体加载器
 * <p>
 * 单一职责：加载用户配置的 TTF 字体（或降级到 PDFBox 内置 Helvetica/Courier），
 * 并对外暴露 regular / bold / mono 三个字体引用，供 PDF 渲染层复用。
 *
 * <p>字体加载失败时降级到 PDFBox 内置 Type1 字体，但需注意 Type1 仅支持 Latin-1 字符，
 * 中文需配置 TTF 路径（{@code mujin.document.pdf-export.font-path}）。</p>
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Slf4j
public class PdfBoxFontRegistry {

    /**
     * 普通字体
     */
    private PDFont regularFont;

    /**
     * 加粗字体
     */
    private PDFont boldFont;

    /**
     * 等宽字体（用于代码块）
     */
    private PDFont monoFont;

    /**
     * 是否已加载 TTF
     */
    private boolean usingTrueType;

    /**
     * 加载字体：优先使用 TTF，加载失败时降级到 PDFBox 内置字体
     *
     * @param pdfDoc     PDF 文档
     * @param properties 文档配置属性
     * @throws IOException 字体加载失败时抛出
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public void load(PDDocument pdfDoc, DocumentProperties properties) throws IOException {
        String fontPath = properties.getPdfExport().getFontPath();
        if (fontPath != null && !fontPath.isBlank() && new File(fontPath).exists()) {
            try {
                PDFont baseFont = PDType0Font.load(pdfDoc, new File(fontPath));
                this.regularFont = baseFont;
                this.boldFont = baseFont;
                this.monoFont = baseFont;
                this.usingTrueType = true;
                log.info("已加载 TTF 字体：{}", fontPath);
                return;
            } catch (IOException e) {
                log.warn("加载 TTF 字体失败，降级到 Helvetica：{}", e.getMessage());
            }
        }
        // 降级到 PDFBox 内置的 14 种标准字体
        this.regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        this.boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        this.monoFont = new PDType1Font(Standard14Fonts.FontName.COURIER);
        this.usingTrueType = false;
    }

    /**
     * 获取普通字体
     *
     * @return PDFont 普通字体
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public PDFont getRegularFont() {
        return regularFont;
    }

    /**
     * 获取加粗字体
     *
     * @return PDFont 加粗字体
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public PDFont getBoldFont() {
        return boldFont;
    }

    /**
     * 获取等宽字体（用于代码块）
     *
     * @return PDFont 等宽字体
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public PDFont getMonoFont() {
        return monoFont;
    }

    /**
     * 是否使用 TTF 字体（决定是否需要 Unicode 清洗）
     *
     * @return boolean true 表示使用 TTF
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public boolean isUsingTrueType() {
        return usingTrueType;
    }
}

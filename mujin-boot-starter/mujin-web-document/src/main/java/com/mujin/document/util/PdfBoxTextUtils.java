package com.mujin.document.util;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * PDFBox 文本处理工具类
 * <p>
 * 提供文本绘制、截断、清洗、字体度量等通用方法。
 *
 * @author chenglin.wu
 * @date 2026/08/17
 */
@SuppressWarnings("unused")
public final class PdfBoxTextUtils {

    /**
     * 私有构造，禁止实例化
     */
    private PdfBoxTextUtils() {
    }

    /**
     * 绘制单行文本
     *
     * @param cs    内容流
     * @param text  文本
     * @param font  字体
     * @param size  字号
     * @param x     X 坐标
     * @param y     Y 坐标
     * @throws IOException IO 异常
     */
    public static void drawText(org.apache.pdfbox.pdmodel.PDPageContentStream cs,
                                String text,
                                PDFont font,
                                float size,
                                float x,
                                float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text, font));
        cs.endText();
    }

    /**
     * 绘制多行文本（自动换行）
     *
     * @param cs        内容流
     * @param text      文本
     * @param font      字体
     * @param size      字号
     * @param x         X 起始坐标
     * @param y         Y 起始坐标
     * @param maxWidth  最大宽度
     * @param lineHeight 行高倍数（如 1.2f）
     * @return float 绘制后的新 Y 坐标
     * @throws IOException IO 异常
     */
    public static float drawMultilineText(org.apache.pdfbox.pdmodel.PDPageContentStream cs,
                                          String text,
                                          PDFont font,
                                          float size,
                                          float x,
                                          float y,
                                          float maxWidth,
                                          float lineHeight) throws IOException {
        if (text == null || text.isBlank()) {
            return y;
        }

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        float lineWidth = 0;

        for (String word : words) {
            String testLine = line.isEmpty() ? word : line + " " + word;
            float testWidth = font.getStringWidth(testLine) / 1000f * size;

            if (testWidth > maxWidth && !line.isEmpty()) {
                // 绘制当前行
                cs.beginText();
                cs.setFont(font, size);
                cs.newLineAtOffset(x, y);
                cs.showText(sanitize(line.toString(), font));
                cs.endText();
                y -= size * lineHeight;

                // 开始新行
                line = new StringBuilder(word);
                lineWidth = font.getStringWidth(word) / 1000f * size;
            } else {
                line.append(line.isEmpty() ? "" : " ").append(word);
                lineWidth = testWidth;
            }
        }

        // 绘制最后一行
        if (!line.isEmpty()) {
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, y);
            cs.showText(sanitize(line.toString(), font));
            cs.endText();
            y -= size * lineHeight;
        }

        return y;
    }

    /**
     * 文本清洗：根据字体类型过滤不可显示字符
     *
     * @param text 原始文本
     * @param font 目标字体
     * @return String 清洗后的文本
     */
    public static String sanitize(String text, PDFont font) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Type1 内置字体（Helvetica/Courier）仅支持 Latin-1
        if (font instanceof PDType1Font) {
            StringBuilder sb = new StringBuilder(text.length());
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 256) {
                    sb.append(c);
                } else {
                    sb.append('?');
                }
            }
            return sb.toString();
        }

        // TTF 字体由字体自身处理，仅移除控制字符
        return text.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "");
    }

    /**
     * 按字符数截断文本
     *
     * @param text   原始文本
     * @param maxLen 最大字符数
     * @return String 截断后的文本（超长时加省略号）
     */
    public static String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLen - 1) + "…";
    }

    /**
     * 计算文本宽度
     *
     * @param text  文本
     * @param font  字体
     * @param size  字号
     * @return float 宽度（磅）
     */
    public static float stringWidth(String text, PDFont font, float size) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        try {
            return font.getStringWidth(text) / 1000f * size;
        } catch (IOException e) {
            return 0f;
        }
    }

    /**
     * 计算文本高度
     *
     * @param font 字体
     * @param size 字号
     * @return float 高度（磅）
     */
    public static float fontHeight(PDFont font, float size) {
        try {
            return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * size;
        } catch (IOException e) {
            return size * 1.2f;
        }
    }
}
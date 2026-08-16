package com.mujin.document.util.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

/**
 * PDFBox 表格渲染器
 * <p>
 * 单一职责：渲染表格（表头 + 数据行），支持：
 * <ul>
 *   <li>列宽按比例自适应</li>
 *   <li>表头跨页自动重复（由调用方在 {@link PdfBoxLayoutEngine} 中驱动换页）</li>
 *   <li>单元格文本截断</li>
 * </ul>
 *
 * <p>设计约束：本类不维护流（{@code PDPageContentStream}）引用，
 * 每次调用都由调用方传入 cs，且由调用方决定何时调用 {@link PdfBoxLayoutEngine#newPage()}。
 * 调用方标准流程：</p>
 * <pre>{@code
 *   // 1. 首表头
 *   renderer.drawHeader(cs, layout, fontReg, headers, colWidths, marginX);
 *   // 2. 循环数据行
 *   for (String[] row : rows) {
 *       if (layout.needsNewPage()) {
 *           cs.close();
 *           cs = new PDPageContentStream(pdfDoc, layout.newPage());
 *           renderer.drawHeader(cs, layout, fontReg, headers, colWidths, marginX);
 *       }
 *       renderer.drawRow(cs, layout, fontReg, row, colWidths, marginX);
 *   }
 * }</pre>
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Slf4j
public class PdfBoxTableRenderer {

    /**
     * 表头行高（磅）
     */
    private static final float HEADER_HEIGHT = 14f;

    /**
     * 数据行行高（磅）
     */
    private static final float ROW_HEIGHT = 12f;

    /**
     * 表头字号
     */
    private static final float HEADER_FONT_SIZE = 9f;

    /**
     * 数据行字号
     */
    private static final float ROW_FONT_SIZE = 9f;

    /**
     * 单格最大字符数
     */
    private static final int CELL_MAX_LEN = 30;

    /**
     * 计算绝对列宽（按比例切分总宽）
     *
     * @param widths     列宽比例（任意单位）
     * @param totalWidth 总可绘制宽度
     * @return float[] 绝对列宽
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public float[] computeColumnWidths(float[] widths, float totalWidth) {
        float totalRatio = 0;
        for (float w : widths) {
            totalRatio += w;
        }
        float[] colWidths = new float[widths.length];
        for (int i = 0; i < widths.length; i++) {
            colWidths[i] = widths[i] / totalRatio * totalWidth;
        }
        return colWidths;
    }

    /**
     * 绘制表头行（粗体）
     *
     * @param cs        内容流
     * @param layout    布局引擎
     * @param fontReg   字体注册表
     * @param headers   表头数组
     * @param colWidths 绝对列宽（与 {@link #computeColumnWidths} 返回值对应）
     * @param x         X 起始坐标
     * @throws IOException IO 异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public void drawHeader(PDPageContentStream cs,
                           PdfBoxLayoutEngine layout,
                           PdfBoxFontRegistry fontReg,
                           String[] headers,
                           float[] colWidths,
                           float x) throws IOException {
        float cursor = x;
        PDFont boldFont = fontReg.getBoldFont();
        for (int i = 0; i < headers.length; i++) {
            PdfBoxTextUtils.drawText(cs, headers[i], boldFont, HEADER_FONT_SIZE, cursor, layout.getCurrentY());
            cursor += colWidths[i];
        }
        layout.advanceY(HEADER_HEIGHT);
    }

    /**
     * 绘制一行数据
     *
     * @param cs        内容流
     * @param layout    布局引擎
     * @param fontReg   字体注册表
     * @param row       数据行
     * @param colWidths 绝对列宽
     * @param x         X 起始坐标
     * @throws IOException IO 异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public void drawRow(PDPageContentStream cs,
                        PdfBoxLayoutEngine layout,
                        PdfBoxFontRegistry fontReg,
                        String[] row,
                        float[] colWidths,
                        float x) throws IOException {
        float cursor = x;
        PDFont regularFont = fontReg.getRegularFont();
        for (int i = 0; i < row.length && i < colWidths.length; i++) {
            String cell = row[i] != null ? row[i] : "";
            PdfBoxTextUtils.drawText(cs, truncate(cell, CELL_MAX_LEN),
                    regularFont, ROW_FONT_SIZE, cursor, layout.getCurrentY());
            cursor += colWidths[i];
        }
        layout.advanceY(ROW_HEIGHT);
    }

    /**
     * 按字符数截断文本
     *
     * @param text   原始文本
     * @param maxLen 最大字符数
     * @return String 截断后的文本
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public static String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLen - 1) + "…";
    }
}

package com.mujin.document.util.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * PDFBox 布局状态管理器
 * <p>
 * 单一职责：跟踪页面尺寸、边距、当前 Y 坐标，
 * 并管理 PDF 文档的页面列表。
 * 不直接操作 {@code PDPageContentStream}，仅维护布局上下文。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@SuppressWarnings("unused")
public class PdfBoxLayoutEngine {

    /**
     * A4 页面尺寸（单位：磅）
     */
    private static final PDRectangle A4 = new PDRectangle(595, 842);

    /**
     * LETTER 页面尺寸
     */
    private static final PDRectangle LETTER = new PDRectangle(612, 792);

    /**
     * 默认边距（磅，约 20mm）
     */
    private static final float DEFAULT_MARGIN_MM = 20f;

    /**
     * 毫米 → 磅 换算系数
     */
    private static final float MM_TO_POINT = 2.83465f;

    /**
     * 当前 PDF 文档
     */
    private final PDDocument pdfDoc;

    /**
     * 当前页面的页面尺寸
     */
    private PDRectangle pageSize;

    /**
     * 当前 Y 坐标（从页面顶部开始）
     */
    private float currentY;

    /**
     * 边距（磅）
     */
    private float margin;

    /**
     * 当前页
     */
    private PDPage currentPage;

    /**
     * 已创建页面列表
     */
    private final List<PDPage> pages = new ArrayList<>();

    public PdfBoxLayoutEngine(PDDocument pdfDoc) {
        this.pdfDoc = pdfDoc;
        this.pageSize = A4;
        this.margin = DEFAULT_MARGIN_MM * MM_TO_POINT;
        this.currentY = pageSize.getHeight() - this.margin;
    }

    /**
     * 根据字符串返回页面尺寸，支持 "A4" / "LETTER"，默认 A4
     *
     * @param pageSizeName 页面尺寸名
     * @return PDRectangle 页面尺寸
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public static PDRectangle resolvePageSize(String pageSizeName) {
        if ("LETTER".equalsIgnoreCase(pageSizeName)) {
            return LETTER;
        }
        return A4;
    }

    /**
     * 设置页面尺寸与边距
     *
     * @param pageSizeName 页面尺寸（A4 / LETTER）
     * @param marginMm     边距（毫米）
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public void configure(String pageSizeName, float marginMm) {
        this.pageSize = resolvePageSize(pageSizeName);
        this.margin = marginMm * MM_TO_POINT;
        this.currentY = pageSize.getHeight() - this.margin;
    }

    /**
     * 新建一页并切换当前 Y 坐标到顶部
     *
     * @return PDPage 新建的页面
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public PDPage newPage() {
        this.currentPage = new PDPage(pageSize);
        pdfDoc.addPage(currentPage);
        pages.add(currentPage);
        this.currentY = pageSize.getHeight() - margin;
        return currentPage;
    }

    /**
     * 推进 Y 坐标（向下移动）
     *
     * @param dy 推进距离（磅，正数向下）
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public void advanceY(float dy) {
        this.currentY -= dy;
    }

    /**
     * 设置当前 Y 坐标
     *
     * @param y Y 坐标
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public void setCurrentY(float y) {
        this.currentY = y;
    }

    /**
     * 获取当前 Y 坐标
     *
     * @return float Y 坐标
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public float getCurrentY() {
        return currentY;
    }

    /**
     * 获取左边距 X 坐标
     *
     * @return float X 坐标
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public float getMarginX() {
        return margin;
    }

    /**
     * 获取可绘制区域宽度
     *
     * @return float 宽度（磅）
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public float getContentWidth() {
        return pageSize.getWidth() - 2 * margin;
    }

    /**
     * 获取可绘制区域高度
     *
     * @return float 高度（磅）
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public float getContentHeight() {
        return pageSize.getHeight() - 2 * margin;
    }

    /**
     * 获取页面尺寸
     *
     * @return PDRectangle 页面尺寸
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public PDRectangle getPageSize() {
        return pageSize;
    }

    /**
     * 获取当前页
     *
     * @return PDPage 当前页
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public PDPage getCurrentPage() {
        return currentPage;
    }

    /**
     * 判断当前 Y 坐标是否低于底边距（需要换页）
     *
     * @return boolean true 表示需要换页
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public boolean needsNewPage() {
        return currentY < margin;
    }
}

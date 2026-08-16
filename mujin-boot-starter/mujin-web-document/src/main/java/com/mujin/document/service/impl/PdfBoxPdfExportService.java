package com.mujin.document.service.impl;

import com.mujin.document.configuration.DocumentProperties;
import com.mujin.document.model.ApiDocument;
import com.mujin.document.model.ApiEndpoint;
import com.mujin.document.model.ApiModel;
import com.mujin.document.model.ApiParameter;
import com.mujin.document.model.ApiResponse;
import com.mujin.document.model.ApiSecurityScheme;
import com.mujin.document.model.CodeExample;
import com.mujin.document.model.ExportRequest;
import com.mujin.document.service.PdfExportService;
import com.mujin.document.util.pdf.PdfBoxFontRegistry;
import com.mujin.document.util.pdf.PdfBoxLayoutEngine;
import com.mujin.document.util.pdf.PdfBoxTableRenderer;
import com.mujin.document.util.pdf.PdfBoxTextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Apache PDFBox 3.x 的 PDF 导出服务实现
 * <p>
 * 本类仅做顶层编排（封面 / 目录 / 章节 / 模型），具体的字体加载、布局计算、表格渲染
 * 委托给 {@link PdfBoxFontRegistry} / {@link PdfBoxLayoutEngine} / {@link PdfBoxTableRenderer}。
 * <p>
 * Apache PDFBox 使用 Apache License 2.0，可免费用于商业闭源产品，无传染性风险，
 * 适合作为 iText 7（AGPL）的替代方案。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Slf4j
@SuppressWarnings("unused")
public class PdfBoxPdfExportService implements PdfExportService {

    /**
     * 字体大小：标题
     */
    private static final float SIZE_TITLE = 24.0f;

    /**
     * 字体大小：副标题
     */
    private static final float SIZE_SUBTITLE = 14.0f;

    /**
     * 字体大小：章节标题
     */
    private static final float SIZE_HEADING = 16.0f;

    /**
     * 字体大小：二级标题
     */
    private static final float SIZE_SUBHEADING = 12.0f;

    /**
     * 字体大小：正文
     */
    private static final float SIZE_BODY = 10.0f;

    /**
     * 字体大小：表格 / 代码块
     */
    private static final float SIZE_TABLE = 9.0f;

    /**
     * 章节标题间距
     */
    private static final float GAP_SECTION = 18.0f;

    /**
     * 配置属性
     */
    private final DocumentProperties properties;

    /**
     * 表格渲染器
     */
    private final PdfBoxTableRenderer tableRenderer = new PdfBoxTableRenderer();

    public PdfBoxPdfExportService(DocumentProperties properties) {
        this.properties = properties;
    }

    /**
     * 导出 PDF 到输出流
     *
     * @param document    API 文档模型
     * @param request     导出请求参数
     * @param outputStream 输出流
     * @throws Exception 导出异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Override
    public void exportPdf(ApiDocument document, ExportRequest request, OutputStream outputStream) throws Exception {
        try (PDDocument pdfDoc = new PDDocument()) {
            PdfBoxFontRegistry fontReg = new PdfBoxFontRegistry();
            fontReg.load(pdfDoc, properties);

            PdfBoxLayoutEngine layout = new PdfBoxLayoutEngine(pdfDoc);
            layout.configure(request.getPageSize(), request.getMargin());

            // 应用分组/标签/已弃用过滤
            ApiDocument filtered = filterDocument(document, request);

            // 1. 封面页
            renderCoverPage(pdfDoc, layout, fontReg, filtered, request);

            // 2. 目录
            renderTableOfContents(pdfDoc, layout, fontReg, filtered);

            // 3. 接口详情
            if (filtered.getEndpoints() != null) {
                for (ApiEndpoint endpoint : filtered.getEndpoints()) {
                    renderEndpoint(pdfDoc, layout, fontReg, endpoint, request);
                }
            }

            // 4. 数据模型
            if (request.isIncludeModels() && filtered.getModels() != null && !filtered.getModels().isEmpty()) {
                renderModelsSection(pdfDoc, layout, fontReg, filtered.getModels());
            }

            // 5. 安全方案
            if (filtered.getSecuritySchemes() != null && !filtered.getSecuritySchemes().isEmpty()) {
                renderSecuritySchemesSection(pdfDoc, layout, fontReg, filtered.getSecuritySchemes());
            }

            pdfDoc.save(outputStream);
            log.info("PDF 导出完成：{} 个接口，{} 个模型，{} 个安全方案",
                    filtered.getEndpoints() != null ? filtered.getEndpoints().size() : 0,
                    filtered.getModels() != null ? filtered.getModels().size() : 0,
                    filtered.getSecuritySchemes() != null ? filtered.getSecuritySchemes().size() : 0);
        }
    }

    /**
     * 导出 PDF 到文件
     *
     * @param document API 文档模型
     * @param request  导出请求参数
     * @param filePath 文件路径
     * @throws Exception 导出异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Override
    public void exportPdfToFile(ApiDocument document, ExportRequest request, String filePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            exportPdf(document, request, fos);
        }
    }

    /**
     * 导出 PDF 并返回字节数组
     *
     * @param document API 文档模型
     * @param request  导出请求参数
     * @return byte[] PDF 字节内容
     * @throws Exception 导出异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Override
    public byte[] exportToBytes(ApiDocument document, ExportRequest request) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            exportPdf(document, request, baos);
            return baos.toByteArray();
        }
    }

    /**
     * 获取引擎名称
     *
     * @return String 引擎名称
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Override
    public String getEngineName() {
        return "PDFBox3";
    }

    /**
     * 按 ExportRequest 过滤文档（分组 / 标签 / 已弃用）
     *
     * @param document 原始文档
     * @param request 导出请求
     * @return ApiDocument 过滤后的文档（不影响原对象）
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private ApiDocument filterDocument(ApiDocument document, ExportRequest request) {
        ApiDocument filtered = new ApiDocument();
        filtered.setTitle(document.getTitle());
        filtered.setVersion(document.getVersion());
        filtered.setDescription(document.getDescription());
        filtered.setGeneratedAt(document.getGeneratedAt());
        filtered.setTags(document.getTags());
        filtered.setGroups(document.getGroups());
        filtered.setModels(document.getModels());
        filtered.setSecuritySchemes(document.getSecuritySchemes());

        if (document.getEndpoints() == null) {
            filtered.setEndpoints(new ArrayList<>());
            return filtered;
        }

        List<ApiEndpoint> endpoints = new ArrayList<>();
        for (ApiEndpoint endpoint : document.getEndpoints()) {
            if (!request.isIncludeDeprecated() && endpoint.isDeprecated()) {
                continue;
            }
            if (request.getGroups() != null && !request.getGroups().isEmpty()) {
                if (endpoint.getGroup() == null || !request.getGroups().contains(endpoint.getGroup())) {
                    continue;
                }
            }
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                if (endpoint.getTags() == null
                        || endpoint.getTags().stream().noneMatch(request.getTags()::contains)) {
                    continue;
                }
            }
            endpoints.add(endpoint);
        }
        filtered.setEndpoints(endpoints);
        return filtered;
    }

    /**
     * 渲染封面页
     *
     * @param pdfDoc   PDF 文档
     * @param layout   布局引擎
     * @param fontReg  字体注册表
     * @param document API 文档模型
     * @param request  导出请求
     * @throws IOException IO 异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private void renderCoverPage(PDDocument pdfDoc,
                                 PdfBoxLayoutEngine layout,
                                 PdfBoxFontRegistry fontReg,
                                 ApiDocument document,
                                 ExportRequest request) throws IOException {
        PDPage page = layout.newPage();
        PDRectangle pageSize = layout.getPageSize();
        float pageWidth = pageSize.getWidth();

        try (PDPageContentStream cs = new PDPageContentStream(pdfDoc, page)) {
            PDFont boldFont = fontReg.getBoldFont();
            PDFont regularFont = fontReg.getRegularFont();

            // 标题
            String title = document.getTitle() != null ? document.getTitle() : "API 接口文档";
            float titleWidth = PdfBoxTextUtils.stringWidth(title, boldFont, SIZE_TITLE);
            cs.beginText();
            cs.setFont(boldFont, SIZE_TITLE);
            cs.newLineAtOffset((pageWidth - titleWidth) / 2, 700);
            cs.showText(PdfBoxTextUtils.sanitize(title, boldFont));
            cs.endText();

            // 副标题
            if (document.getDescription() != null) {
                String subtitle = document.getDescription();
                float subWidth = PdfBoxTextUtils.stringWidth(subtitle, regularFont, SIZE_SUBTITLE);
                cs.beginText();
                cs.setFont(regularFont, SIZE_SUBTITLE);
                cs.newLineAtOffset((pageWidth - subWidth) / 2, 660);
                cs.showText(PdfBoxTextUtils.sanitize(subtitle, regularFont));
                cs.endText();
            }

            // 元信息
            int endpointCount = document.getEndpoints() != null ? document.getEndpoints().size() : 0;
            int modelCount = document.getModels() != null ? document.getModels().size() : 0;
            int schemeCount = document.getSecuritySchemes() != null ? document.getSecuritySchemes().size() : 0;
            String[][] info = {
                    {"版本", document.getVersion() != null ? document.getVersion() : "-"},
                    {"生成时间", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))},
                    {"接口总数", String.valueOf(endpointCount)},
                    {"模型总数", String.valueOf(modelCount)},
                    {"安全方案", String.valueOf(schemeCount)},
                    {"导出格式", request.getFormat()},
                    {"生成引擎", getEngineName()}
            };
            float startY = 580;
            for (String[] row : info) {
                PdfBoxTextUtils.drawText(cs, row[0] + "：", boldFont, SIZE_BODY, 180, startY);
                PdfBoxTextUtils.drawText(cs, row[1], regularFont, SIZE_BODY, 280, startY);
                startY -= 20;
            }
        }
    }

    /**
     * 渲染目录
     *
     * @param pdfDoc   PDF 文档
     * @param layout   布局引擎
     * @param fontReg  字体注册表
     * @param document API 文档模型
     * @throws IOException IO 异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private void renderTableOfContents(PDDocument pdfDoc,
                                       PdfBoxLayoutEngine layout,
                                       PdfBoxFontRegistry fontReg,
                                       ApiDocument document) throws IOException {
        PDPage page = layout.newPage();
        try (PDPageContentStream cs = new PDPageContentStream(pdfDoc, page)) {
            PDFont boldFont = fontReg.getBoldFont();
            PDFont regularFont = fontReg.getRegularFont();

            PdfBoxTextUtils.drawText(cs, "目录", boldFont, SIZE_HEADING, layout.getMarginX(), 780);
            float y = 750;
            int index = 1;
            if (document.getEndpoints() != null) {
                for (ApiEndpoint endpoint : document.getEndpoints()) {
                    String line = String.format("%d. %s %s - %s", index++,
                            endpoint.getMethod().toUpperCase(),
                            endpoint.getPath(),
                            endpoint.getSummary() != null ? endpoint.getSummary() : "");
                    PdfBoxTextUtils.drawText(cs, PdfBoxTextUtils.truncate(line, 80),
                            regularFont, SIZE_TABLE, layout.getMarginX() + 20, y);
                    y -= 16;
                    if (y < 60) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * 渲染单个接口详情
     *
     * @param pdfDoc   PDF 文档
     * @param layout   布局引擎
     * @param fontReg  字体注册表
     * @param endpoint 接口端点
     * @param request  导出请求
     * @throws IOException IO 异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private void renderEndpoint(PDDocument pdfDoc,
                                PdfBoxLayoutEngine layout,
                                PdfBoxFontRegistry fontReg,
                                ApiEndpoint endpoint,
                                ExportRequest request) throws IOException {
        layout.configure(request.getPageSize(), request.getMargin());
        layout.newPage();
        float x = layout.getMarginX();
        PDFont boldFont = fontReg.getBoldFont();
        PDFont regularFont = fontReg.getRegularFont();
        PDFont monoFont = fontReg.getMonoFont();

        try (PDPageContentStream cs = new PDPageContentStream(pdfDoc, layout.getCurrentPage())) {
            // 接口标题
            String title = endpoint.getMethod().toUpperCase() + "  " + endpoint.getPath();
            PdfBoxTextUtils.drawText(cs, title, boldFont, SIZE_SUBHEADING, x, layout.getCurrentY());
            layout.advanceY(20);

            // 摘要
            if (endpoint.getSummary() != null) {
                PdfBoxTextUtils.drawText(cs, "摘要：" + endpoint.getSummary(),
                        regularFont, SIZE_BODY, x, layout.getCurrentY());
                layout.advanceY(16);
            }
            // 描述
            if (endpoint.getDescription() != null && !endpoint.getDescription().equals(endpoint.getSummary())) {
                PdfBoxTextUtils.drawText(cs, "描述：" + endpoint.getDescription(),
                        regularFont, SIZE_BODY, x, layout.getCurrentY());
                layout.advanceY(16);
            }

            // 参数表
            if (endpoint.getParameters() != null && !endpoint.getParameters().isEmpty()) {
                drawSectionTitle(cs, layout, "请求参数", boldFont);
                String[][] rows = endpoint.getParameters().stream().map(p -> new String[]{
                        p.getName(),
                        p.getIn() != null ? p.getIn() : "-",
                        formatType(p),
                        p.isRequired() ? "是" : "否",
                        p.getDescription() != null ? p.getDescription() : ""
                }).toArray(String[][]::new);
                renderTableSinglePage(cs, layout, fontReg,
                        new String[]{"名称", "位置", "类型", "必填", "说明"},
                        new float[]{1.2f, 0.8f, 1.2f, 0.6f, 2.5f},
                        rows, x);
            }

            // 响应
            if (endpoint.getResponses() != null && !endpoint.getResponses().isEmpty()) {
                drawSectionTitle(cs, layout, "响应", boldFont);
                for (ApiResponse resp : endpoint.getResponses()) {
                    PdfBoxTextUtils.drawText(cs, "HTTP " + resp.getStatusCode() + " - "
                                    + (resp.getDescription() != null ? resp.getDescription() : ""),
                            boldFont, SIZE_TABLE, x, layout.getCurrentY());
                    layout.advanceY(14);
                }
            }

            // 代码示例（单页模式，超长自动截断）
            if (request.isIncludeExamples() && endpoint.getExamples() != null && !endpoint.getExamples().isEmpty()) {
                drawSectionTitle(cs, layout, "调用示例", boldFont);
                for (CodeExample example : endpoint.getExamples()) {
                    PdfBoxTextUtils.drawText(cs, example.getLanguage().toUpperCase(),
                            boldFont, SIZE_TABLE, x, layout.getCurrentY());
                    layout.advanceY(14);
                    String code = example.getCode() != null ? example.getCode() : "";
                    for (String line : code.split("\n")) {
                        if (layout.needsNewPage()) {
                            break;
                        }
                        PdfBoxTextUtils.drawText(cs,
                                PdfBoxTextUtils.truncate(line, 90),
                                monoFont, SIZE_TABLE - 1, x + 10, layout.getCurrentY());
                        layout.advanceY(12);
                    }
                    layout.advanceY(6);
                }
            }
        }
    }

    /**
     * 单页表格渲染（不跨页，长度超出时仅截断不报错）
     *
     * @param cs        内容流
     * @param layout    布局引擎
     * @param fontReg   字体注册表
     * @param headers   表头
     * @param widths    列宽比例
     * @param rows      数据行
     * @param x         X 起始坐标
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private void renderTableSinglePage(PDPageContentStream cs,
                                       PdfBoxLayoutEngine layout,
                                       PdfBoxFontRegistry fontReg,
                                       String[] headers,
                                       float[] widths,
                                       String[][] rows,
                                       float x) throws IOException {
        float[] colWidths = tableRenderer.computeColumnWidths(widths, layout.getContentWidth());
        tableRenderer.drawHeader(cs, layout, fontReg, headers, colWidths, x);
        for (String[] row : rows) {
            if (layout.needsNewPage()) {
                break;
            }
            tableRenderer.drawRow(cs, layout, fontReg, row, colWidths, x);
        }
        layout.advanceY(6);
    }

    /**
     * 绘制章节标题（粗体 + 间距）
     *
     * @param cs      内容流
     * @param layout  布局引擎
     * @param title   标题
     * @param boldFont 粗体字体
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private void drawSectionTitle(PDPageContentStream cs,
                                  PdfBoxLayoutEngine layout,
                                  String title,
                                  PDFont boldFont) throws IOException {
        PdfBoxTextUtils.drawText(cs, title, boldFont,
                SIZE_SUBHEADING, layout.getMarginX(), layout.getCurrentY());
        layout.advanceY(GAP_SECTION);
    }

    /**
     * 渲染数据模型章节
     *
     * @param pdfDoc  PDF 文档
     * @param layout  布局引擎
     * @param fontReg 字体注册表
     * @param models  数据模型列表
     * @throws IOException IO 异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private void renderModelsSection(PDDocument pdfDoc,
                                     PdfBoxLayoutEngine layout,
                                     PdfBoxFontRegistry fontReg,
                                     List<ApiModel> models) throws IOException {
        layout.newPage();
        try (PDPageContentStream cs = new PDPageContentStream(pdfDoc, layout.getCurrentPage())) {
            PDFont boldFont = fontReg.getBoldFont();
            PDFont regularFont = fontReg.getRegularFont();

            PdfBoxTextUtils.drawText(cs, "数据模型", boldFont, SIZE_HEADING, layout.getMarginX(), 780);
            layout.setCurrentY(750);

            for (ApiModel model : models) {
                if (layout.needsNewPage()) {
                    break;
                }
                PdfBoxTextUtils.drawText(cs, model.getName(), boldFont, SIZE_SUBHEADING,
                        layout.getMarginX(), layout.getCurrentY());
                layout.advanceY(18);
                if (model.getDescription() != null) {
                    PdfBoxTextUtils.drawText(cs, model.getDescription(),
                            regularFont, SIZE_BODY, layout.getMarginX(), layout.getCurrentY());
                    layout.advanceY(14);
                }
                if (model.getProperties() != null && !model.getProperties().isEmpty()) {
                    String[][] rows = model.getProperties().stream().map(p -> new String[]{
                            p.getName(),
                            formatType(p),
                            model.getRequired() != null && model.getRequired().contains(p.getName()) ? "是" : "否",
                            p.getDescription() != null ? p.getDescription() : ""
                    }).toArray(String[][]::new);
                    float[] widths = {1.2f, 1.5f, 0.6f, 2.5f};
                    float[] colWidths = tableRenderer.computeColumnWidths(widths, layout.getContentWidth());
                    tableRenderer.drawHeader(cs, layout, fontReg,
                            new String[]{"字段", "类型", "必填", "说明"}, colWidths, layout.getMarginX());
                    for (String[] row : rows) {
                        if (layout.needsNewPage()) {
                            break;
                        }
                        tableRenderer.drawRow(cs, layout, fontReg, row, colWidths, layout.getMarginX());
                    }
                    layout.advanceY(10);
                }
            }
        }
    }

    /**
     * 渲染安全方案章节
     *
     * @param pdfDoc   PDF 文档
     * @param layout   布局引擎
     * @param fontReg  字体注册表
     * @param schemes  安全方案列表
     * @throws IOException IO 异常
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private void renderSecuritySchemesSection(PDDocument pdfDoc,
                                              PdfBoxLayoutEngine layout,
                                              PdfBoxFontRegistry fontReg,
                                              List<ApiSecurityScheme> schemes) throws IOException {
        layout.newPage();
        try (PDPageContentStream cs = new PDPageContentStream(pdfDoc, layout.getCurrentPage())) {
            PDFont boldFont = fontReg.getBoldFont();

            PdfBoxTextUtils.drawText(cs, "安全方案", boldFont, SIZE_HEADING, layout.getMarginX(), 780);
            layout.setCurrentY(750);

            String[][] rows = schemes.stream().map(s -> new String[]{
                    s.getName() != null ? s.getName() : "-",
                    s.getType() != null ? s.getType() : "-",
                    s.getDescription() != null ? s.getDescription() : "",
                    formatSchemeDetail(s)
            }).toArray(String[][]::new);

            float[] widths = {1.2f, 1.0f, 2.5f, 2.5f};
            float[] colWidths = tableRenderer.computeColumnWidths(widths, layout.getContentWidth());
            tableRenderer.drawHeader(cs, layout, fontReg,
                    new String[]{"名称", "类型", "说明", "详情"},
                    colWidths, layout.getMarginX());
            for (String[] row : rows) {
                tableRenderer.drawRow(cs, layout, fontReg, row, colWidths, layout.getMarginX());
            }
        }
    }

    /**
     * 格式化安全方案详情
     *
     * @param s 安全方案
     * @return String 详情字符串
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private String formatSchemeDetail(ApiSecurityScheme s) {
        StringBuilder sb = new StringBuilder();
        if (s.getScheme() != null) {
            sb.append("scheme=").append(s.getScheme());
        }
        if (s.getBearerFormat() != null) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("bearer=").append(s.getBearerFormat());
        }
        if (s.getOpenIdConnectUrl() != null) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("oidc=").append(s.getOpenIdConnectUrl());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    /**
     * 格式化参数类型显示
     *
     * @param param 参数模型
     * @return String 格式化后的类型字符串
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private String formatType(ApiParameter param) {
        StringBuilder sb = new StringBuilder();
        if (param.getType() != null) {
            sb.append(param.getType());
        }
        if (param.getFormat() != null) {
            sb.append("(").append(param.getFormat()).append(")");
        }
        if (param.getSchemaRef() != null) {
            sb.append(" → ").append(param.getSchemaRef());
        }
        return sb.toString();
    }
}

package com.mujin.document.service;

import com.mujin.document.model.ApiDocument;
import com.mujin.document.model.ExportRequest;

import java.io.OutputStream;

/**
 * PDF 导出服务接口
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
public interface PdfExportService {

    /**
     * 导出 PDF 到输出流
     *
     * @param document   API 文档模型
     * @param request    导出请求参数
     * @param outputStream 输出流
     * @throws Exception 导出异常
     */
    void exportPdf(ApiDocument document, ExportRequest request, OutputStream outputStream) throws Exception;

    /**
     * 导出 PDF 到文件
     *
     * @param document API 文档模型
     * @param request  导出请求参数
     * @param filePath 文件路径
     * @throws Exception 导出异常
     */
    void exportPdfToFile(ApiDocument document, ExportRequest request, String filePath) throws Exception;

    /**
     * 导出 PDF 并返回字节数组
     *
     * @param document API 文档模型
     * @param request  导出请求参数
     * @return byte[] PDF 字节内容
     * @throws Exception 导出异常
     */
    byte[] exportToBytes(ApiDocument document, ExportRequest request) throws Exception;

    /**
     * 获取引擎名称
     *
     * @return 引擎名称
     */
    String getEngineName();
}
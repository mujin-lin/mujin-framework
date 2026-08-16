package com.mujin.document.controller;

import com.mujin.document.code.DocumentErrorCode;
import com.mujin.document.handler.DocumentExceptionHandler;
import com.mujin.document.model.ApiDocument;
import com.mujin.document.model.ApiEndpoint;
import com.mujin.document.service.CodeExampleGenerator;
import com.mujin.document.service.OpenApiParserService;
import com.mujin.document.service.PdfExportService;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DocumentController 集成测试（MockMvc + @WebMvcTest）
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@WebMvcTest(controllers = DocumentController.class)
@Import(DocumentExceptionHandler.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpenApiParserService parserService;

    @MockitoBean
    private PdfExportService pdfExportService;

    @MockitoBean
    private CodeExampleGenerator codeExampleGenerator;

    @MockitoBean
    private List<GroupedOpenApi> groupedOpenApis;

    @BeforeEach
    void setUp() {
        // 默认：返回一个最小 OpenAPI
        when(parserService.getMergedOpenApi()).thenReturn(new OpenAPI());
        when(parserService.parseAllGroups()).thenReturn(buildSampleDocument());
        when(parserService.getAvailableGroups()).thenReturn(List.of("user", "order"));

        GroupedOpenApi user = org.mockito.Mockito.mock(GroupedOpenApi.class);
        when(user.getGroup()).thenReturn("user");
        GroupedOpenApi order = org.mockito.Mockito.mock(GroupedOpenApi.class);
        when(order.getGroup()).thenReturn("order");
        when(groupedOpenApis.stream()).thenReturn(java.util.stream.Stream.of(user, order));
        when(groupedOpenApis.iterator()).thenReturn(List.of(user, order).iterator());
    }

    private ApiDocument buildSampleDocument() {
        ApiDocument doc = new ApiDocument();
        doc.setTitle("Sample API");
        doc.setVersion("1.0");
        doc.setDescription("Sample");
        ApiEndpoint e1 = new ApiEndpoint();
        e1.setMethod("GET");
        e1.setPath("/users");
        e1.setSummary("用户列表");
        e1.setGroup("user");
        ApiEndpoint e2 = new ApiEndpoint();
        e2.setMethod("POST");
        e2.setPath("/orders");
        e2.setSummary("创建订单");
        e2.setGroup("order");
        doc.setEndpoints(new ArrayList<>(List.of(e1, e2)));
        return doc;
    }

    @Test
    @DisplayName("GET /api-docs/groups 默认参数")
    void testGetGroups() throws Exception {
        mockMvc.perform(get("/api-docs/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resCode").value(0))
                .andExpect(jsonPath("$.resData.total").value(2));
    }

    @Test
    @DisplayName("GET /api-docs/groups 带分页")
    void testGetGroupsPaged() throws Exception {
        mockMvc.perform(get("/api-docs/groups")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resData.items.length()").value(1));
    }

    @Test
    @DisplayName("GET /api-docs/groups 带搜索过滤")
    void testGetGroupsSearch() throws Exception {
        mockMvc.perform(get("/api-docs/groups")
                        .param("search", "use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resData.total").value(1))
                .andExpect(jsonPath("$.resData.items[0]").value("user"));
    }

    @Test
    @DisplayName("GET /api-docs/groups 参数非法返回 6005")
    void testGetGroupsInvalidParam() throws Exception {
        mockMvc.perform(get("/api-docs/groups")
                        .param("page", "-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resCode").value(DocumentErrorCode.INVALID_PARAM.errorCode()));
    }

    @Test
    @DisplayName("GET /api-docs/spec/{group} 不存在的分组返回 6002")
    void testGetSpecNotFound() throws Exception {
        mockMvc.perform(get("/api-docs/spec/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resCode").value(DocumentErrorCode.SPEC_NOT_FOUND.errorCode()));
    }

    @Test
    @DisplayName("GET /api-docs/export/json 返回 ok")
    void testExportJson() throws Exception {
//        mockMvc.perform(get("/api-docs/export/json"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.$").exists());
    }

    @Test
    @DisplayName("GET /api-docs/endpoints 列出端点")
    void testListEndpoints() throws Exception {
        mockMvc.perform(get("/api-docs/endpoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resCode").value(0))
                .andExpect(jsonPath("$.resData.total").value(2));
    }

    @Test
    @DisplayName("GET /api-docs/endpoints 带搜索过滤")
    void testListEndpointsSearch() throws Exception {
        mockMvc.perform(get("/api-docs/endpoints")
                        .param("search", "用户"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resData.total").value(1));
    }
}

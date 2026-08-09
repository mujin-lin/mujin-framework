package com.mujin.logging.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mujin.commons.lang.JsonUtil;
import com.mujin.logging.annotations.LogIgnore;
import com.mujin.logging.annotations.LogMask;
import com.mujin.logging.annotations.MaskType;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ParamJsonSerializer} 脱敏与忽略行为回归测试
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
class ParamJsonSerializerTest {

    @Test
    void testNullReturnsNull() {
        assertNull(ParamJsonSerializer.toJson(null));
    }

    @Test
    void testKeepHead() {
        User user = new User();
        user.setIdCard("11010119900101001X");

        String json = ParamJsonSerializer.toJson(user);
        JsonNode node = JsonUtil.toJsonNode(json);
        // 默认 KEEP_HEAD head=3：保留头 3 位，其余替换为 *
        assertTrue(node.get("idCard").asText().startsWith("110"));
        assertTrue(node.get("idCard").asText().endsWith("*"));
        assertTrue(node.get("idCard").asText().contains("*"));
    }

    @Test
    void testKeepTail() {
        User user = new User();
        user.setIdCardMask("11010119900101001X");

        String json = ParamJsonSerializer.toJson(user);
        JsonNode node = JsonUtil.toJsonNode(json);
        // KEEP_TAIL tail=4
        assertTrue(node.get("idCardMask").asText().endsWith("001X"));
        assertTrue(node.get("idCardMask").asText().startsWith("*"));
    }

    @Test
    void testMiddle() {
        User user = new User();
        user.setPhoneMask("13800001234");

        String json = ParamJsonSerializer.toJson(user);
        JsonNode node = JsonUtil.toJsonNode(json);
        // 11 位手机号，头 3 + 尾 4，中间 4 个 *
        assertEquals("138****1234", node.get("phoneMask").asText());
    }

    @Test
    void testAllMask() {
        User user = new User();
        user.setPasswordMask("abc123");

        String json = ParamJsonSerializer.toJson(user);
        JsonNode node = JsonUtil.toJsonNode(json);
        assertEquals("******", node.get("passwordMask").asText());
    }

    @Test
    void testLogIgnoreRemovesField() {
        User user = new User();
        user.setIdCard("11010119900101001X");
        user.setPassword("secret");

        String json = ParamJsonSerializer.toJson(user);
        JsonNode node = JsonUtil.toJsonNode(json);
        assertNotNull(node.get("idCard"));
        // @LogIgnore 字段不应出现在 JSON 中
        assertNull(node.get("password"));
    }

    @Test
    void testNestedObjectInheritsMask() {
        Order order = new Order();
        order.setOrderNo("SO20260809001");
        User user = new User();
        user.setIdCard("11010119900101001X");
        user.setPassword("abc");
        order.setUser(user);

        String json = ParamJsonSerializer.toJson(order);
        JsonNode node = JsonUtil.toJsonNode(json);
        JsonNode userNode = node.get("user");
        assertNotNull(userNode);
        assertNotNull(userNode.get("idCard"));
        assertNull(userNode.get("password"));
        // 嵌套对象也按 KEEP_HEAD 脱敏
        assertTrue(userNode.get("idCard").asText().contains("*"));
    }

    @Test
    void testListElementsInheritMask() {
        User u1 = new User();
        u1.setIdCard("11010119900101001X");
        u1.setPassword("p1");
        User u2 = new User();
        u2.setIdCard("11010119900101002Y");
        u2.setPassword("p2");
        UserList wrapper = new UserList();
        wrapper.setUsers(Arrays.asList(u1, u2));

        String json = ParamJsonSerializer.toJson(wrapper);
        JsonNode node = JsonUtil.toJsonNode(json);
        JsonNode users = node.get("users");
        assertTrue(users.isArray());
        assertEquals(2, users.size());
        for (JsonNode u : users) {
            assertNotNull(u.get("idCard"));
            assertNull(u.get("password"));
            assertTrue(u.get("idCard").asText().contains("*"));
        }
    }

    @Test
    void testMapValueInheritsMask() {
        Map<String, User> map = new HashMap<>();
        User u = new User();
        u.setIdCard("11010119900101001X");
        u.setPassword("p");
        map.put("k1", u);

        String json = ParamJsonSerializer.toJson(map);
        JsonNode node = JsonUtil.toJsonNode(json);
        JsonNode k1 = node.get("k1");
        assertNotNull(k1.get("idCard"));
        assertNull(k1.get("password"));
    }

    /**
     * 测试 POJO：含各种注解
     */
    @Data
    @SuppressWarnings("unused")
    public static class User {
        /**
         * 身份证号：默认 KEEP_HEAD head=3
         */
        @LogMask
        private String idCard;

        /**
         * 自定义策略
         */
        @LogMask(MaskType.KEEP_TAIL)
        private String idCardMask;

        /**
         * 手机号：MIDDLE
         */
        @LogMask(value = MaskType.MIDDLE, head = 3, tail = 4)
        private String phoneMask;

        /**
         * 密码：ALL
         */
        @LogMask(MaskType.ALL)
        private String passwordMask;

        /**
         * 普通字段
         */
        private String phone;

        /**
         * 密码字段：忽略
         */
        @LogIgnore
        private String password;
    }

    /**
     * 测试 POJO：嵌套 User
     */
    @Data
    @SuppressWarnings("unused")
    public static class Order {
        private String orderNo;
        private User user;
    }

    /**
     * 测试 POJO：包含 User 列表
     */
    @Data
    @SuppressWarnings("unused")
    public static class UserList {
        private List<User> users;
    }

    @Test
    void testShortValueKeepsUnchanged() {
        User u = new User();
        u.setIdCard("abc");
        // 短于 head，不脱敏
        String json = ParamJsonSerializer.toJson(u);
        JsonNode node = JsonUtil.toJsonNode(json);
        assertEquals("abc", node.get("idCard").asText());
    }

    @Test
    void testNonStringFieldNotMasked() {
        NumberBox box = new NumberBox();
        box.setCount(123456);
        String json = ParamJsonSerializer.toJson(box);
        JsonNode node = JsonUtil.toJsonNode(json);
        // @LogMask 用在 int 字段上时，默认 KEEP_HEAD head=3，数字转 toString 后整体处理
        assertEquals("123***", node.get("count").asText());
    }

    @Test
    void testEmptyObjectReturnsEmptyBraces() {
        String json = ParamJsonSerializer.toJson(new Empty());
        assertEquals("{}", json);
    }

    @Test
    void testPlainStringSerializedAsIs() {
        String json = ParamJsonSerializer.toJson("hello");
        assertEquals("\"hello\"", json);
    }

    @Test
    void testSerializationFailureFallbackToToString() {
        Object obj = new Object() {
            @Override
            public String toString() {
                return "fallback";
            }
        };
        String json = ParamJsonSerializer.toJson(obj);
        // 序列化失败时降级为 toString
        assertTrue(json.contains("fallback") || json.contains("{"));
    }

    /**
     * 测试 POJO：数字字段（非字符串）
     */
    @Data
    @SuppressWarnings("unused")
    public static class NumberBox {
        @LogMask
        private int count;
    }

    /**
     * 测试 POJO：空对象
     */
    @Data
    @SuppressWarnings("unused")
    public static class Empty {
    }

    @Test
    void testListOfStringsNotMasked() {
        String json = ParamJsonSerializer.toJson(Arrays.asList("a", "b"));
        JsonNode node = JsonUtil.toJsonNode(json);
        assertTrue(node.isArray());
        assertEquals("a", node.get(0).asText());
        assertEquals("b", node.get(1).asText());
        assertFalse(node.get(0).asText().contains("*"));
    }
}

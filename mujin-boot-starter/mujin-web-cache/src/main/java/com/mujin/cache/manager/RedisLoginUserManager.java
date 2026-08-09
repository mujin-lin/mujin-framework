package com.mujin.cache.manager;

import cn.hutool.core.util.StrUtil;
import com.mujin.commons.web.manager.LoginUserManager;
import com.mujin.commons.web.model.LoginUserModel;
import com.mujin.commons.web.utils.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author chenglin.wu
 * @date 2026/5/6
 */
public class RedisLoginUserManager implements LoginUserManager {

    private final Logger log = LoggerFactory.getLogger(RedisLoginUserManager.class);


    private final StringRedisTemplate stringRedisTemplate;

    private final RedisTemplate<String, LoginUserModel> redisTemplate;

    private final String CACHE_NUM_SUFFIX = "_cache_num";


    public RedisLoginUserManager(StringRedisTemplate stringRedisTemplate, RedisTemplate<String, LoginUserModel> redisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 添加用户到redis中
     *
     * @param loginUser 登录对象
     * @date 2026/05/06 16:17
     **/
    @Override
    public void setLoginUser(LoginUserModel loginUser) {
        Assert.notNull(loginUser.getCacheId(), "token为userId，设置Redis用户失败");
        Assert.notNull(loginUser.getToken(), "token为null，设置Redis用户失败");

        if (loginUser.isSingleLogin()) {
            // 检查redis中是否存储当前用户，如果有则删除，并重新添加
            LoginUserModel oldLoginUser = this.getLoginUser(loginUser.getCacheId());
            if (oldLoginUser != null) {
                this.deleteLoginUser(oldLoginUser.getToken());
            }
        }

        log.debug("RedisLoginUserManager createLoginUser login name:{} , login token:{} , login id:{}", loginUser.getUserName(), loginUser.getToken(), loginUser.getCacheId());

        // 添加token
        stringRedisTemplate.opsForValue().set(loginUser.getToken(), loginUser.getCacheId(),
                Duration.ofMinutes(30L));
        // 添加登录对象
        redisTemplate.opsForValue().set(loginUser.getCacheId(), loginUser, Duration.ofMinutes(30L));

        this.putCacheIdNum(loginUser.getCacheId());

    }

    /**
     * 通过请求对象获取 token
     *
     * @param request 请求对象
     * @return LoginUser
     * @date 2024/06/08
     */
    @Override
    public <T> LoginUserModel getLoginUserByRequest(T request) {
        String token = TokenUtil.getTokenFromServlet(request);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return this.getLoginUserByToken(token);
    }

    /**
     * 获取redis中的LoginUser
     *
     * @param token 登录令牌
     * @return LoginUserModel
     * @date 2026/05/06 16:17
     **/
    @Override
    public LoginUserModel getLoginUserByToken(String token) {
        // 判断token是否为空，为空则返回null
        if (StrUtil.isBlank(token)) {
            return null;
        }
        String loginUserId = this.getCacheId(token);
        // 判断登录对象是否为空，为空则返回null
        if (StrUtil.isBlank(loginUserId)) {
            return null;
        }
        try {
            return this.getLoginUser(loginUserId);
        } catch (Exception e) {
            log.debug("get user has error: ", e);
            return null;
        }
    }

    /**
     * 删除redis中的user
     *
     * @param token 登录的令牌
     * @author Y
     * @date 2026/05/06 16:17
     **/
    @Override
    public void deleteLoginUser(String token) {
        String cacheId = this.getCacheId(token);
        if (StrUtil.isNotBlank(cacheId)) {
            redisTemplate.delete(cacheId);
        }
        stringRedisTemplate.delete(token);
        this.decrOrRemoveCacheIdNum(cacheId);
    }

    @Override
    public void refreshLoginTime(String token) {
        // 过期时间
        Long expire = stringRedisTemplate.getExpire(token, TimeUnit.SECONDS);

        String cacheId = this.getCacheId(token);

        // 需要更新expire
        if (expire < Duration.ofMinutes(30L).getSeconds()) {
            stringRedisTemplate.expire(token, Duration.ofMinutes(30L));
            redisTemplate.expire(cacheId, Duration.ofMinutes(30L));
            stringRedisTemplate.expire(cacheId + CACHE_NUM_SUFFIX, Duration.ofMinutes(30L));
        }
    }


    /**
     * 通过userId获取redis中的LoginUser
     *
     * @param userId 用户id
     * @return LoginUserModel
     * @date 2026/05/06
     **/
    @Override
    public LoginUserModel getLoginUser(String userId) {
        if (redisTemplate == null) {
            log.error("RedisLoginUserManager getLoginUserByUserId stringRedisTemplate is null");
            return null;
        } else {
            return redisTemplate.opsForValue().get(userId);
        }
    }

    @Override
    public String getCacheId(String token) {
        return stringRedisTemplate.opsForValue().get(token);
    }

    /**
     * 从 redis 获取 userId
     *
     * @param token 登录令牌
     * @author chenglin.wu
     * @date 2026/05/06 14:23
     **/
    public String getLoginUserId(String token) {
        log.debug("RedisLoginUserManager getLoginUserId token" + token);
        if (stringRedisTemplate == null) {
            log.error("RedisLoginUserManager getLoginUserId stringRedisTemplate is null");
        } else {
            Object obj = stringRedisTemplate.opsForValue().get(token);
            if (obj != null) {
                return obj.toString();
            }
        }
        return null;
    }

    /**
     * 通过 id 批量删除登录对象
     *
     * @param redisId the redisId
     * @author chenglin.wu
     * @date  2026/05/06
     */
    @Override
    public void deleteLoginUserById(String... redisId) {
        log.debug("RedisLoginUserManager deleteLoginUserById id:{}", Arrays.toString(redisId));
        if (Objects.isNull(redisTemplate)) {
            log.error("RedisLoginUserManager deleteLoginUserById redisTemplate is null");
        } else {
            for (String id : redisId) {
                LoginUserModel obj = redisTemplate.opsForValue().get(id);
                if (Objects.nonNull(obj)) {
                    this.deleteLoginUser(obj.getToken());
                }
            }
        }
    }

    /**
     * 添加或者 incr 当前 cache id 的登录数量
     *
     * @param cacheId 缓存 id
     * @date  2026/05/06
     */
    private void putCacheIdNum(String cacheId) {
        String luaScript = "local cacheId = KEYS[1]\n" +
                "local setVal = ARGV[1]\n" +
                "local timeOutSecond = ARGV[2]\n" +
                "local cacheVal = redis.call('EXISTS',cacheId)\n" +
                "if (cacheVal == 1) then\n" +
                "   redis.call('INCR',cacheId)\n" +
                "else \n" +
                "   redis.call('SETEX',cacheId,timeOutSecond,setVal)\n" +
                "end";

        // 键
        List<String> keys = Collections.singletonList(cacheId + CACHE_NUM_SUFFIX);
        // 脚本对象
        DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>(luaScript);
        stringRedisTemplate.execute(redisScript, keys, "1", String.valueOf(Duration.ofMinutes(30L).getSeconds()));
    }

    /**
     * 删除或递减cache id的数量
     *
     * @param cacheId 缓存id
     * @date 2024/11/19
     */
    private void decrOrRemoveCacheIdNum(String cacheId) {
        String luaScript = "local cacheId = KEYS[1]\n" +
                "local cacheVal = redis.call('EXISTS',cacheId)\n" +
                "if (cacheVal == 1) \n" +
                "then\n" +
                "  local decrVal = redis.call('DECR',cacheId)\n" +
                "  if(decrVal <= 0) \n" +
                "    then\n" +
                "       redis.call('DEL',cacheId)\n" +
                "   end\n" +
                "end";
        // 键
        List<String> keys = Collections.singletonList(cacheId + CACHE_NUM_SUFFIX);
        // 脚本对象
        DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>(luaScript);
        stringRedisTemplate.execute(redisScript, keys);
    }
}

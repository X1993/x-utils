package com.github.util.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import static com.github.util.concurrent.ReadWriteSwitchConstant.*;

/**
 * 读写切换操作
 *
 * @author X1993
 * @date 2022/11/25
 * @description
 */
@Slf4j
public class ReadWriteSwitchOperatorImpl implements ReadWriteSwitchOperator {

    private final String separator;

    private final String pointerKeySuffix;

    private final String writeLockKeySuffix;

    private final PointerKeyOperator pointerKeyOperator;

    private final LockOperator lockOperator;

    public ReadWriteSwitchOperatorImpl(String separator,
                                       String pointerKeySuffix,
                                       String writeLockKeySuffix,
                                       PointerKeyOperator pointerKeyOperator,
                                       LockOperator lockOperator)
    {
        Objects.requireNonNull(pointerKeyOperator);
        Objects.requireNonNull(lockOperator);

        this.pointerKeyOperator = pointerKeyOperator;
        this.lockOperator = lockOperator;
        this.separator = StringUtils.isEmpty(separator) ? DEFAULT_SEPARATOR : separator;
        this.pointerKeySuffix = StringUtils.isEmpty(pointerKeySuffix) ? DEFAULT_POINTER_KEY_SUFFIX : pointerKeySuffix;
        this.writeLockKeySuffix = StringUtils.isEmpty(writeLockKeySuffix) ? DEFAULT_WRITE_LOCK_KEY_SUFFIX : writeLockKeySuffix;
    }

    public ReadWriteSwitchOperatorImpl(PointerKeyOperator pointerKeyOperator,
                                       LockOperator lockOperator)
    {
        this(null ,null ,null ,pointerKeyOperator ,lockOperator);
    }

    @Override
    public String getReadKey(String key){
        return getOffsetKey(key ,getReadOffset(getPointerOffset(key)));
    }

    //获取映射的写锁键
    private String getWriteLockKey(String key){
        return key + writeLockKeySuffix;
    }

    @Override
    public boolean write(String key, Long maxBlockingMS, Long lockExpiredMS,
                         Consumer<String> writeOperator, Consumer<String> delOperator)
    {
        String writeLockKey = getWriteLockKey(key);
        String occupierId = UUID.randomUUID().toString();

        boolean isLockSuccess = lockOperator.tryLock(writeLockKey, occupierId, lockExpiredMS, maxBlockingMS);
        if (!isLockSuccess){
            return false;
        }

        log.debug("get writeLock:{},occupierId:{},lockTimeoutMS:{}" ,
                writeLockKey ,occupierId , lockExpiredMS);

        try {
            Integer pointerOffset = getPointerOffset(key);
            Integer readOffset = getReadOffset(pointerOffset);
            int writeOffset = getWriteOffset(pointerOffset);

            String writeKey = getOffsetKey(key ,writeOffset);
            //写入新数据区
            writeOperator.accept(writeKey);

            String pointerKey = getPointerKey(key);
            //指针从旧数据区切换到新数据区
            pointerKeyOperator.setOffset(pointerKey ,writeOffset);

            if (readOffset != null && delOperator != null){
                String oldWriteKey = getOffsetKey(key, readOffset);
                //删除旧数据（失败了也没关系，下次覆盖写入即可）
                delOperator.accept(oldWriteKey);
            }
            return true;
        } catch (Exception e){
            log.error("write exception" ,e);
            return false;
        } finally {
            // 释放锁
            boolean releaseResult = lockOperator.releaseLock(writeLockKey, occupierId);
            if (releaseResult) {
                log.debug("release writeLock:{},occupierId:{}",
                        writeLockKey, occupierId);
            }
        }
    }

    private String getPointerKey(String key){
        return key + pointerKeySuffix;
    }

    private String getOffsetKey(String key ,int pointerOffset){
        return key + separator + pointerOffset;
    }

    private Integer getPointerOffset(String key){
        if (StringUtils.isEmpty(key)){
            return null;
        }
        return pointerKeyOperator.getOffset(getPointerKey(key));
    }

    private Integer getReadOffset(Integer pointerOffset){
        return pointerOffset == null ? null : (pointerOffset == 0 ? 0 : 1);
    }

    private int getWriteOffset(Integer pointerOffset){
        // 0/1 切换
        return pointerOffset == null ? 0 : (pointerOffset == 0 ? 1 : 0);
    }

}

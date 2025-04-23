package com.xxl.job.core.biz.model;

import java.io.Serializable;

/**
 * @author jiangwangfa
 * @date 2025/4/23
 * @description
 */
public class PriorityParam implements Serializable {
    private static final long serialVersionUID = 42000001L;
    /**
     * 是否优先执行
     */
    private boolean priority;

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }
}

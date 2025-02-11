package com.xxl.job.core.biz.model;

import java.io.Serializable;

/**
 * @author jiangwangfa
 * @date 2025/2/6
 * @description
 */
public class ParentParam implements Serializable {

    private static final long serialVersionUID = 1024005L;
    /**
     * 是否继承父任务参数
     */
    private boolean applyParentParam;

    public boolean isApplyParentParam() {
        return applyParentParam;
    }

    public void setApplyParentParam(boolean applyParentParam) {
        this.applyParentParam = applyParentParam;
    }
}

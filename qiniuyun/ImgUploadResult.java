package com.ydcloud.shop.gateway.qiniu;

import java.math.BigDecimal;

public class ImgUploadResult {

    /**
     * 是否上传成功
     */
    private boolean success;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片尺寸，单位byte
     */
    private BigDecimal size;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

}

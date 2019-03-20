package com.ydcloud.shop.gateway.qiniu;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwx.framework.util.DateUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

public class QiNiuApi {

    private static Logger logger = LoggerFactory.getLogger(QiNiuApi.class);

    private String accessKey;

    private String secretKey;

    private String token;

    private UploadManager uploadManager;
    // 要上传的空间
    private String bucketname;

    /**
     * token过期时间
     */
    private Date expiredTime;

    // 上传之后保存文件url的前缀，前缀+upload方法的fileName参数可以构成一个可访问的文件url
    private String urlPrefix;

    public QiNiuApi(String accessKey, String secretKey, String bucketName, String urlPrefix) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketname = bucketName;
        this.urlPrefix = urlPrefix;

        Auth auth = Auth.create(this.accessKey, this.secretKey);
        this.token = auth.uploadToken(this.bucketname);
        this.expiredTime = DateUtil.addMinutes(new Date(), 40);
        Zone z = Zone.autoZone();
        Configuration c = new Configuration(z);
        this.uploadManager = new UploadManager(c);
    }

    public ImgUploadResult upload(byte[] data, String fileName) {
        ImgUploadResult result = new ImgUploadResult();
        result.setSuccess(true);
        if (null == data) {
            result.setSize(BigDecimal.ZERO);
            logger.error("上传到七牛的文件内容为Null,fileName{}.", fileName);

            return result;
        }
        try {
            // 调用put方法上传
            Response res = uploadManager.put(data, fileName, token);
            // 打印返回的信息
            if (null != res && res.isOK()) {
                result.setUrl(urlPrefix + fileName);
                result.setSize(new BigDecimal(data.length));
            } else {
                logger.error("上传文件到七牛失败,reponse={},response.error={}.,", null != res ? res : "res为null",
                        null != res ? res.error : "");
                result.setSuccess(false);
                result.setSize(BigDecimal.ZERO);
            }
        } catch (QiniuException e) {
            Response r = e.response;
            try {
                logger.error("上传文件到七牛失败,reponse={}:", r.bodyString(), e);
            } catch (QiniuException e1) {
                logger.error("上传文件到七牛失败:", e);
            }
            result.setSuccess(false);
            result.setSize(BigDecimal.ZERO);
        } catch (Exception e) {
            logger.error("上传文件到七牛失败:", e);
            result.setSuccess(false);
            result.setSize(BigDecimal.ZERO);
        }

        return result;
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }

    /**
     * token是否已过期
     * 
     * @return
     */
    public boolean isExpired() {
        return new Date().compareTo(expiredTime) > 0;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketname() {
        return bucketname;
    }

    public void setBucketname(String bucketname) {
        this.bucketname = bucketname;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

}

package com.ydcloud.shop.gateway.qiniu;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.hwx.framework.base.exception.ServiceException;

public class QiNiuFactory {

    private static Map<String, QiNiuApi> qiNiuApiContainer = new HashMap<String, QiNiuApi>();

    private static final String accessKey_KEY = "accessKey";

    private static final String secretKey_KEY = "secretKey";

    private static final String bucketName_KEY = "bucketName";

    private static final String urlPrefix_KEY = "urlPrefix";

    public static QiNiuApi getQiNiuApi(String qiNiuParam) {

        if (StringUtils.isBlank(qiNiuParam)) {
            throw new ServiceException("", "图片服务器参数无效");
        }

        JSONObject json = JSONObject.parseObject(qiNiuParam);

        String accessKey = json.getString(accessKey_KEY);

        String secretKey = json.getString(secretKey_KEY);

        String bucketName = json.getString(bucketName_KEY);

        String urlPrefix = json.getString(urlPrefix_KEY);

        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey) || StringUtils.isBlank(bucketName)
                || StringUtils.isBlank(urlPrefix)) {
            throw new ServiceException("", "图片服务器参数无效");
        }
        if (null == qiNiuApiContainer.get(qiNiuParam)) {
            QiNiuApi qiNiuApi = new QiNiuApi(accessKey, secretKey, bucketName, urlPrefix);
            qiNiuApiContainer.put(qiNiuParam, qiNiuApi);

            return qiNiuApi;
        } else {
            QiNiuApi qiNiuApi = qiNiuApiContainer.get(qiNiuParam);
            if (qiNiuApi.isExpired()) {
                qiNiuApi = new QiNiuApi(accessKey, secretKey, bucketName, urlPrefix);
                qiNiuApiContainer.put(qiNiuParam, qiNiuApi);
            }
            return qiNiuApi;
        }

    }

}

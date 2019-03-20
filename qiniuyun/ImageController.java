package com.ydcloud.shop.gateway.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcloud.common.ApiResult;
import com.hwx.framework.base.exception.ServiceException;
import com.hwx.framework.base.util.Assert;
import com.hwx.framework.util.DateUtil;
import com.ydcloud.shop.gateway.qiniu.ImgUploadResult;
import com.ydcloud.shop.gateway.qiniu.QiNiuApi;
import com.ydcloud.shop.gateway.qiniu.QiNiuFactory;
import com.ydcloud.shop.gateway.service.OauthUserdetailService;
import com.ydcloud.shop.system.dto.UserSession;
import com.ydcloud.shop.system.enums.SystemParamEnums;
import com.ydcloud.shop.system.service.SystemParamService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "图片上传接口")
@RestController
@RequestMapping("/image")
public class ImageController {

    private static Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private SystemParamService systemParamService;

    @Autowired
    private OauthUserdetailService oauthUserdetailService;

    /** 允许上传的图片格式 */
    private static final String[] ENABLE_UPLOAD_EXT = new String[] { "bmp", "jpg", "gif", "png", "jpeg", "tga", "pcd" };

    /** 允许上传的图片大小10M */
    private static final long MAX_UPLOAD_SIZE = 10 * 1024 * 1024l;

    /**
     * 上传图片
     *
     * @param
     * @return
     */
    @ApiOperation(value = "接口：上传图片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uploadFile", value = "上传的文件", required = true, paramType = "form", dataType = "string", allowableValues = "o2o,lease,es"), })
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ApiResult<Map<String, String>> upload(@ApiIgnore OAuth2Authentication auth,
            @RequestParam(required = false, value = "uploadFile") MultipartFile[] uploadFile,
            HttpServletRequest request, HttpServletResponse response) {

        ApiResult<Map<String, String>> result = new ApiResult<Map<String, String>>();

        UserSession user = oauthUserdetailService.getCurrentUser(auth);

        Map<String, String> resultMap = new HashMap<String, String>();
        if (null == uploadFile) {
            throw new ServiceException(Assert.PARAM_INVALID, "上传的图片不能为空");
        }
        if (null != uploadFile && uploadFile.length > 0) {
            for (MultipartFile currentFile : uploadFile) {
                uploadFile(currentFile, resultMap, user);
            }
        }
        return result.buildSuccess(resultMap);
    }

    private void uploadFile(MultipartFile file, Map<String, String> resultMap, UserSession user) {
        // 图片大小超过限制
        long size = file.getSize();
        if (size > MAX_UPLOAD_SIZE) {
            throw new ServiceException(Assert.PARAM_INVALID, "上传的图片大小超过系统最大限制：10M");
        }

        // 图片格式不对
        String origName = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(origName).toLowerCase(Locale.ENGLISH);
        if (!ArrayUtils.contains(ENABLE_UPLOAD_EXT, ext)) {
            throw new ServiceException(Assert.PARAM_INVALID, "请上传系统规定格式的图片");
        }

        String qiNiuParam = systemParamService.queryStringByKey(SystemParamEnums.QI_NIU_IMG_UPLOAD_PARAM.getParamKey(),
                "");

        BigDecimal byteSize = BigDecimal.ZERO;

        String datePattern = DateUtil.date2Str(new Date(), "yyyyMMdd");

        try {
            byte[] data = file.getBytes();

            QiNiuApi uploadApi = QiNiuFactory.getQiNiuApi(qiNiuParam);

            if (null == data) {
                throw new ServiceException("", "请选择有效的图片上传");
            }
            // 文件名格式:会员编号/日期/毫秒+随机数.
            String fileName = user.getId() + "/" + datePattern + "/" + String.valueOf(System.currentTimeMillis())
                    + (int) Math.floor(Math.random() * 10) + "." + ext;

            ImgUploadResult uploadResult = uploadApi.upload(data, fileName);

            if (uploadResult.isSuccess()) {
                resultMap.put(origName, uploadResult.getUrl());
                byteSize = byteSize.add(uploadResult.getSize());
            } else {
                throw new ServiceException("", "图片上传失败");
            }
        } catch (IOException e) {
            logger.error("图片上传失败:", e);
            throw new ServiceException("", "图片上传失败");
        }
    }

}

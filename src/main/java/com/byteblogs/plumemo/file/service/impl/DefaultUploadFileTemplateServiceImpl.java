package com.byteblogs.plumemo.file.service.impl;

import com.byteblogs.common.cache.ConfigCache;
import com.byteblogs.common.constant.Constants;
import com.byteblogs.common.constant.ConstantsModels;
import com.byteblogs.common.util.FileUtil;
import com.byteblogs.plumemo.file.factory.UploadFileFactory;
import com.byteblogs.plumemo.file.service.UploadFileTemplateService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class DefaultUploadFileTemplateServiceImpl implements UploadFileTemplateService, InitializingBean {

    @Override
    public boolean doCheck(final MultipartFile file) {
        return true;
    }

    @Override
    public String doSaveFileStore(final MultipartFile file) {
        final String filePath = ConstantsModels.getDefaultPath(ConfigCache.getConfig(Constants.DEFAULT_PATH));
        String dir = null;
        if(filePath.startsWith(File.separator))
            dir = Paths.get(filePath).toString()+ File.separator;
        else
            dir = Paths.get(System.getProperty("user.home"),filePath).toString()+ File.separator;
        final String fileName = FileUtil.createSingleFileName(file.getOriginalFilename());
        try {
            final File destFile = new File(dir);
            if (!destFile.exists()) {
                destFile.mkdirs();
            }
            file.transferTo(new File(dir + fileName));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return ConfigCache.getConfig(Constants.DEFAULT_IMAGE_DOMAIN)+Constants.FILE_URL+fileName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        UploadFileFactory.register(Constants.DEFAULT_TYPE, this);
    }


}

package com.byteblogs.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.byteblogs.common.cache.ConfigCache;
import com.byteblogs.common.constant.Constants;
import com.byteblogs.common.constant.ConstantsModels;
import com.byteblogs.plumemo.config.dao.ConfigDao;
import com.byteblogs.plumemo.config.domain.po.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    @Autowired
    private ConfigDao configDao;

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        String defaultPath;
        try {
            Config config = configDao.selectOne(new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, Constants.DEFAULT_PATH));
            defaultPath = config.getConfigValue();
        } catch (Exception e) {
            defaultPath = ConstantsModels.getDefaultPath(ConfigCache.getConfig(Constants.DEFAULT_PATH));
        }
        if(defaultPath.startsWith(File.separator)){
            String dir = "file:" + Paths.get(defaultPath).toString() + File.separator;
            registry.addResourceHandler("/files/**").addResourceLocations(dir);
        }else{
            String dir = "file:" + Paths.get(System.getProperty("user.home"),defaultPath).toString()+ File.separator;
            registry.addResourceHandler("/files/**").addResourceLocations(dir);
        }

    }
}
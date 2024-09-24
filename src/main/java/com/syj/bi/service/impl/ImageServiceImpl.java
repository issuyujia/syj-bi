package com.syj.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syj.bi.model.entity.Image;
import com.syj.bi.service.ImageService;
import com.syj.bi.mapper.ImageMapper;
import org.springframework.stereotype.Service;

/**
* @author 山兮
* @description 针对表【image(图片分析表)】的数据库操作Service实现
* @createDate 2024-09-24 13:45:17
*/
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
    implements ImageService{

}





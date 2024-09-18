package com.syj.bi.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syj.bi.annotation.AuthCheck;
import com.syj.bi.common.BaseResponse;
import com.syj.bi.common.DeleteRequest;
import com.syj.bi.common.ErrorCode;
import com.syj.bi.common.ResultUtils;
import com.syj.bi.constant.CommonConstant;
import com.syj.bi.constant.UserConstant;
import com.syj.bi.exception.BusinessException;
import com.syj.bi.exception.ThrowUtils;
import com.syj.bi.manager.AiManager;
import com.syj.bi.manager.RedisLimiterManager;
import com.syj.bi.model.dto.chart.*;
import com.syj.bi.model.entity.Chart;
import com.syj.bi.model.entity.User;
import com.syj.bi.model.vo.BiResponse;
import com.syj.bi.service.ChartService;
import com.syj.bi.service.UserService;
import com.syj.bi.utils.ExcelUtils;
import com.syj.bi.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.syj.bi.common.ResultUtils.success;

/**
 * 队列测试接口
 *
 * @author <a href="https://github.com/lisyj">程序员鱼皮</a>
 * @from <a href="https://syj.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev", "local"})
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name) {
        CompletableFuture.runAsync(() -> {
            System.out.println("任务执行中" + name + ".执行人：" + Thread.currentThread().getName());
            log.info("任务执行中" + name + ".执行人：" + Thread.currentThread().getName());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get() {
        Map<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度", size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务数量", taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成任务数量", completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在执行任务数量", activeCount);
        return JSONUtil.toJsonStr(map);
    }
}

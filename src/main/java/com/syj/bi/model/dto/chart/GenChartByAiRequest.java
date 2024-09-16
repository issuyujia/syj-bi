package com.syj.bi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * @author syj
 * @date 2024/9/9 20:43
 */
@Data
public class GenChartByAiRequest implements Serializable {
    /**
     * 名称
     */
    private String name;

    /**
     * 目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}

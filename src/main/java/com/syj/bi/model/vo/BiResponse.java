package com.syj.bi.model.vo;

/**
 * @author syj
 * @date 2024/9/12 21:43
 */

import lombok.Data;

/**
 * Bi的返回结果
 */
@Data
public class BiResponse {

    private String genChart;

    private String genResult;

    private Long chartId;
}

package com.ie.pdf2.ztools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;


// 拦截器中添加接口请求参数

public class ParameterRequestWrapper extends HttpServletRequestWrapper {
    private Map<String, Object> params = new HashMap<>();

    /**
     * description:  将请求参数放到params中
     * version:
     *
     * @date: 2019/09/30 15:59
     * @author: YangXingfu
     * @param: request http请求
     */
    public ParameterRequestWrapper(HttpServletRequest request) {
        super(request);
        //  获取request参数列表，存放到params中
        this.params.putAll(request.getParameterMap());
    }

    /**
     * description: 将扩展参数放到params中
     * version:
     *
     * @return
     * @date: 2019/09/30 16:02
     * @author: YangXingfu
     * @param: request http请求
     * @param: extendParams
     */
    public ParameterRequestWrapper(HttpServletRequest request, Map<String, Object> extendParams) {
        this(request);
        //这里将扩展参数写入参数表
        addAllParameters(extendParams);
    }

    /**
     * description:  获取参数名，必须重写此方法，不然获取不到添加的参数
     * version:
     *
     * @return java.util.Enumeration<java.lang.String>  参数名迭代器
     * @date: 2019/09/30 16:04
     * @author: YangXingfu
     * @param: null
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(new ArrayList<String>(params.keySet()));
    }

    /**
     * description:  获取String类型参数值
     * version:
     *
     * @return java.lang.String 参数值
     * @date: 2019/09/30 16:05
     * @author: YangXingfu
     * @param: name 参数名
     */
    @Override
    public String getParameter(String name) {
        String[] values = (String[]) params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    /**
     * description:  获取String[]类型参数值
     * version:
     *
     * @return java.lang.String[] 参数值数组
     * @date: 2019/09/30 16:05
     * @author: YangXingfu
     * @param: name 参数名
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = (String[]) params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values;
    }

    /**
     * description:  添加参数和参数值
     * version:
     *
     * @return void
     * @date: 2019/09/30 16:06
     * @author: YangXingfu
     * @param: otherParams 添加的参数和参数值
     */
    public void addAllParameters(Map<String, Object> otherParams) {
        for (Map.Entry<String, Object> entry : otherParams.entrySet()) {
            addParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * description:  将添加的参数名和参数值存放到params中
     * version:
     *
     * @return void
     * @date: 2019/09/30 16:07
     * @author: YangXingfu
     * @param: name 参数名
     * @param: value 参数值
     */
    public void addParameter(String name, Object value) {
        if (value != null) {
            if (value instanceof String[]) {
                params.put(name, (String[]) value);
            } else if (value instanceof String) {
                params.put(name, new String[]{(String) value});
            } else {
                params.put(name, new String[]{String.valueOf(value)});
            }
        }
    }
}

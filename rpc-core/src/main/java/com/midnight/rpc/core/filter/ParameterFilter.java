package com.midnight.rpc.core.filter;

import com.midnight.rpc.core.api.Filter;
import com.midnight.rpc.core.api.RpcContext;
import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;

import java.util.Map;

/**
 * 处理上下文参数
 */
public class ParameterFilter implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.CONTEXT_PARAMETERS.get();
        if(!params.isEmpty()){
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }
}

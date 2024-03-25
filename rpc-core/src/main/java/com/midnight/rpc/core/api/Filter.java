package com.midnight.rpc.core.api;

public interface Filter {

    /**
     * 前置过滤器
     *
     * @param request
     * @return
     */
    Object preFilter(RpcRequest request);

    /**
     * 后置过滤器
     *
     * @param request
     * @param response
     * @param result
     * @return
     */
    Object postFilter(RpcRequest request, RpcResponse response, Object result);


    Filter Default = new Filter() {
        @Override
        public Object preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }
    };
}

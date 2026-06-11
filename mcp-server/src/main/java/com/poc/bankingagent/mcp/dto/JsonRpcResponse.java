package com.poc.bankingagent.mcp.dto;

public record JsonRpcResponse(
        String jsonrpc,
        String id,
        Object result,
        JsonRpcError error
) {
    public static JsonRpcResponse ok(String id, Object result) {
        return new JsonRpcResponse("2.0", id, result, null);
    }

    public static JsonRpcResponse error(String id, int code, String message) {
        return error(id, code, message, null);
    }

    public static JsonRpcResponse error(String id, int code, String message, Object data) {
        return new JsonRpcResponse("2.0", id, null, new JsonRpcError(code, message, data));
    }

    public record JsonRpcError(int code, String message, Object data) {}
}

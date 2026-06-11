package com.poc.bankingagent.mcp.exception;

public class JsonRpcMethodNotFoundException extends RuntimeException {
    public JsonRpcMethodNotFoundException(String method) {
        super("Unsupported method: " + method);
    }
}

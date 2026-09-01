package com.liukx.expression.engine.client.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author liukaixiong
 * @date 2025/11/27 - 17:54
 */
public class AviatorLoggerTraceOutputStream extends OutputStream {

    // 使用 SLF4J 创建日志输出
    private final Logger aviatorLogger = LoggerFactory.getLogger(AviatorLoggerTraceOutputStream.class);


    @Override
    public void write(int b) throws IOException {
        // {@link com.liukx.expression.engine.client.log.AviatorLoggerTraceOutputStream.write(byte[])}
    }

    @Override
    public void write(byte[] b) throws IOException {
        aviatorLogger.info(new String(b));
    }
}

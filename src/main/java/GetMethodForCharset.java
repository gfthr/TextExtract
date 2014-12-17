/*
 * GetMethodForCharset.java
 *
 * Copyright (c) 2014 Chengdu Lanjing Data&Information Co., Ltd
 */

/**
 *
 */

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author sunyanan
 *  对标准org.apache.commons.httpclient.methods.GetMethod的重写，主要是为了覆盖其父类对Charset的探测，这个探测时选用的开源的jar
 */
public class GetMethodForCharset extends GetMethod {

    private Log log = LogFactory.getLog(GetMethodForCharset.class);

    public GetMethodForCharset() {
        super();
    }

    public GetMethodForCharset(String uri) {
        super(uri);
    }

    /**
     * 主要实现的是对这个方法的重写
     */
    public String getResponseCharSet() {
        String charset = getContentCharSet(getResponseHeader("Content-Type"));
        // 默认情况下选择的是 ISO-8859-1，那么就判断如果是这个字符编码的时候再来探测
        if(charset.equalsIgnoreCase("ISO-8859-1")) {
            // 使用组件来判断
            try {
                InputStream is = getResponseBodyAsStream();
                String cs[] = CharsetDetector.getInstance().detectAllCharset(is);
                if(cs != null && cs.length > 0) {
                    charset = cs[0];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.debug("charset used: " + charset);
        return charset;
    }

}
package com.github.jackl.elk.proxy;




import com.github.jackl.elk.core.parser.Parser;
import com.github.jackl.elk.proxy.entity.Proxy;

import java.util.List;


public interface ProxyListPageParser extends Parser {
    /**
     * 是否只要匿名代理
     */
    static final boolean anonymousFlag = true;
    List<Proxy> parse(String content);
}

package com.github.jackl.elk.core.parser;


import com.github.jackl.elk.biz.entity.Page;
import com.github.jackl.elk.biz.entity.User;

public interface DetailPageParser extends Parser {
    User parseDetailPage(Page page);
}

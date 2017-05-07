package com.github.jackl.elk.core.parser;


import com.github.jackl.elk.biz.entity.Page;

import java.util.List;

public interface ListPageParser extends Parser {
    List parseListPage(Page page);
}

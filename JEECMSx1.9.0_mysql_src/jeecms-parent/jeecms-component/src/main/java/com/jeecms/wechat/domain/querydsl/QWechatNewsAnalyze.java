package com.jeecms.wechat.domain.querydsl;

import com.jeecms.wechat.domain.WechatNewsAnalyze;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QWechatNewsAnalyze is a Querydsl query type for WechatNewsAnalyze
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWechatNewsAnalyze extends EntityPathBase<WechatNewsAnalyze> {

    private static final long serialVersionUID = -2091725681L;

    public static final QWechatNewsAnalyze wechatNewsAnalyze = new QWechatNewsAnalyze("wechatNewsAnalyze");

    public final NumberPath<Integer> addToFavCount = createNumber("addToFavCount", Integer.class);

    public final NumberPath<Integer> addToFavUser = createNumber("addToFavUser", Integer.class);

    public final StringPath appId = createString("appId");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> intPageReadCount = createNumber("intPageReadCount", Integer.class);

    public final NumberPath<Integer> intPageReadUser = createNumber("intPageReadUser", Integer.class);

    public final StringPath msgId = createString("msgId");

    public final NumberPath<Integer> oriPageReadCount = createNumber("oriPageReadCount", Integer.class);

    public final NumberPath<Integer> oriPageReadUser = createNumber("oriPageReadUser", Integer.class);

    public final DateTimePath<java.util.Date> refDate = createDateTime("refDate", java.util.Date.class);

    public final NumberPath<Integer> shareCount = createNumber("shareCount", Integer.class);

    public final NumberPath<Integer> shareUser = createNumber("shareUser", Integer.class);

    public final StringPath title = createString("title");

    public QWechatNewsAnalyze(String variable) {
        super(WechatNewsAnalyze.class, forVariable(variable));
    }

    public QWechatNewsAnalyze(Path<? extends WechatNewsAnalyze> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWechatNewsAnalyze(PathMetadata metadata) {
        super(WechatNewsAnalyze.class, metadata);
    }

}


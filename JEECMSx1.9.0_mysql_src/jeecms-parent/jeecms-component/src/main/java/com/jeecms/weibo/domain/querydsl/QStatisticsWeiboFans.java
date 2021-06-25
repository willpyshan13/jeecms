package com.jeecms.weibo.domain.querydsl;

import com.jeecms.weibo.domain.StatisticsWeiboFans;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QStatisticsWeiboFans is a Querydsl query type for StatisticsWeiboFans
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatisticsWeiboFans extends EntityPathBase<StatisticsWeiboFans> {

    private static final long serialVersionUID = -1062777469L;

    public static final QStatisticsWeiboFans statisticsWeiboFans = new QStatisticsWeiboFans("statisticsWeiboFans");

    public final NumberPath<Integer> fansCount = createNumber("fansCount", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.util.Date> statisticsDay = createDateTime("statisticsDay", java.util.Date.class);

    public final StringPath weiboUid = createString("weiboUid");

    public QStatisticsWeiboFans(String variable) {
        super(StatisticsWeiboFans.class, forVariable(variable));
    }

    public QStatisticsWeiboFans(Path<? extends StatisticsWeiboFans> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatisticsWeiboFans(PathMetadata metadata) {
        super(StatisticsWeiboFans.class, metadata);
    }

}


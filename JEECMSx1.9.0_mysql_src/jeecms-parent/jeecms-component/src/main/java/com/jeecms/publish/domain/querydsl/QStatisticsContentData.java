package com.jeecms.publish.domain.querydsl;

import com.jeecms.publish.domain.StatisticsContentData;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QStatisticsContentData is a Querydsl query type for StatisticsContentData
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatisticsContentData extends EntityPathBase<StatisticsContentData> {

    private static final long serialVersionUID = -542192009L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStatisticsContentData statisticsContentData = new QStatisticsContentData("statisticsContentData");

    public final com.jeecms.channel.domain.querydsl.QChannel channel;

    public final NumberPath<Integer> channelId = createNumber("channelId", Integer.class);

    public final NumberPath<Integer> commentCount = createNumber("commentCount", Integer.class);

    public final com.jeecms.content.domain.querydsl.QContent content;

    public final NumberPath<Integer> contentId = createNumber("contentId", Integer.class);

    public final NumberPath<Integer> device = createNumber("device", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final NumberPath<Integer> peopleCount = createNumber("peopleCount", Integer.class);

    public final StringPath province = createString("province");

    public final NumberPath<Integer> readCount = createNumber("readCount", Integer.class);

    public final NumberPath<Integer> siteId = createNumber("siteId", Integer.class);

    public final DateTimePath<java.util.Date> statisticsDay = createDateTime("statisticsDay", java.util.Date.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public QStatisticsContentData(String variable) {
        this(StatisticsContentData.class, forVariable(variable), INITS);
    }

    public QStatisticsContentData(Path<? extends StatisticsContentData> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStatisticsContentData(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStatisticsContentData(PathMetadata metadata, PathInits inits) {
        this(StatisticsContentData.class, metadata, inits);
    }

    public QStatisticsContentData(Class<? extends StatisticsContentData> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.channel = inits.isInitialized("channel") ? new com.jeecms.channel.domain.querydsl.QChannel(forProperty("channel"), inits.get("channel")) : null;
        this.content = inits.isInitialized("content") ? new com.jeecms.content.domain.querydsl.QContent(forProperty("content"), inits.get("content")) : null;
    }

}


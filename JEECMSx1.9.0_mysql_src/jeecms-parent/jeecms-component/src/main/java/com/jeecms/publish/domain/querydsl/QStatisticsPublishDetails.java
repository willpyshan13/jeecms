package com.jeecms.publish.domain.querydsl;

import com.jeecms.publish.domain.StatisticsPublishDetails;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathInits;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QStatisticsPublishDetails is a Querydsl query type for StatisticsPublishDetails
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatisticsPublishDetails extends EntityPathBase<StatisticsPublishDetails> {

    private static final long serialVersionUID = 571172607L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStatisticsPublishDetails statisticsPublishDetails = new QStatisticsPublishDetails("statisticsPublishDetails");

    public final com.jeecms.channel.domain.querydsl.QChannel channel;

    public final NumberPath<Integer> channelId = createNumber("channelId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Long> numbers = createNumber("numbers", Long.class);

    public final com.jeecms.system.domain.querydsl.QCmsOrg org;

    public final NumberPath<Integer> orgId = createNumber("orgId", Integer.class);

    public final com.jeecms.system.domain.querydsl.QCmsSite site;

    public final NumberPath<Integer> siteId = createNumber("siteId", Integer.class);

    public final DateTimePath<java.util.Date> statisticsDay = createDateTime("statisticsDay", java.util.Date.class);

    public final NumberPath<Integer> types = createNumber("types", Integer.class);

    public final com.jeecms.auth.domain.querydsl.QCoreUser user;

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QStatisticsPublishDetails(String variable) {
        this(StatisticsPublishDetails.class, forVariable(variable), INITS);
    }

    public QStatisticsPublishDetails(Path<? extends StatisticsPublishDetails> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStatisticsPublishDetails(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStatisticsPublishDetails(PathMetadata metadata, PathInits inits) {
        this(StatisticsPublishDetails.class, metadata, inits);
    }

    public QStatisticsPublishDetails(Class<? extends StatisticsPublishDetails> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.channel = inits.isInitialized("channel") ? new com.jeecms.channel.domain.querydsl.QChannel(forProperty("channel"), inits.get("channel")) : null;
        this.org = inits.isInitialized("org") ? new com.jeecms.system.domain.querydsl.QCmsOrg(forProperty("org"), inits.get("org")) : null;
        this.site = inits.isInitialized("site") ? new com.jeecms.system.domain.querydsl.QCmsSite(forProperty("site"), inits.get("site")) : null;
        this.user = inits.isInitialized("user") ? new com.jeecms.auth.domain.querydsl.QCoreUser(forProperty("user"), inits.get("user")) : null;
    }

}


package com.jeecms.publish.domain.querydsl;

import com.jeecms.publish.domain.ContentPublishRecord;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathInits;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QContentPublishRecord is a Querydsl query type for ContentPublishRecord
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QContentPublishRecord extends EntityPathBase<ContentPublishRecord> {

    private static final long serialVersionUID = 512653200L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContentPublishRecord contentPublishRecord = new QContentPublishRecord("contentPublishRecord");

    public final com.jeecms.channel.domain.querydsl.QChannel channel;

    public final NumberPath<Integer> channelId = createNumber("channelId", Integer.class);

    public final com.jeecms.content.domain.querydsl.QContent content;

    public final NumberPath<Integer> contentId = createNumber("contentId", Integer.class);

    public final DateTimePath<java.util.Date> publishTime = createDateTime("publishTime", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final com.jeecms.system.domain.querydsl.QCmsOrg org;

    public final NumberPath<Integer> orgId = createNumber("orgId", Integer.class);

    public final com.jeecms.system.domain.querydsl.QCmsSite site;

    public final NumberPath<Integer> siteId = createNumber("siteId", Integer.class);

    public final com.jeecms.auth.domain.querydsl.QCoreUser user;

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QContentPublishRecord(String variable) {
        this(ContentPublishRecord.class, forVariable(variable), INITS);
    }

    public QContentPublishRecord(Path<? extends ContentPublishRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContentPublishRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContentPublishRecord(PathMetadata metadata, PathInits inits) {
        this(ContentPublishRecord.class, metadata, inits);
    }

    public QContentPublishRecord(Class<? extends ContentPublishRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.channel = inits.isInitialized("channel") ? new com.jeecms.channel.domain.querydsl.QChannel(forProperty("channel"), inits.get("channel")) : null;
        this.content = inits.isInitialized("content") ? new com.jeecms.content.domain.querydsl.QContent(forProperty("content"), inits.get("content")) : null;
        this.org = inits.isInitialized("org") ? new com.jeecms.system.domain.querydsl.QCmsOrg(forProperty("org"), inits.get("org")) : null;
        this.site = inits.isInitialized("site") ? new com.jeecms.system.domain.querydsl.QCmsSite(forProperty("site"), inits.get("site")) : null;
        this.user = inits.isInitialized("user") ? new com.jeecms.auth.domain.querydsl.QCoreUser(forProperty("user"), inits.get("user")) : null;
    }

}


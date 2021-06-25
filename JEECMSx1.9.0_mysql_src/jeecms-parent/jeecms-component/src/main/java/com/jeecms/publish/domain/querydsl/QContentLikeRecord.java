package com.jeecms.publish.domain.querydsl;

import com.jeecms.publish.domain.ContentLikeRecord;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QContentLikeRecord is a Querydsl query type for ContentLikeRecord
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QContentLikeRecord extends EntityPathBase<ContentLikeRecord> {

    private static final long serialVersionUID = -1130761352L;

    public static final QContentLikeRecord contentLikeRecord = new QContentLikeRecord("contentLikeRecord");

    public final NumberPath<Integer> channelId = createNumber("channelId", Integer.class);

    public final NumberPath<Integer> contentId = createNumber("contentId", Integer.class);

    public final StringPath cookie = createString("cookie");

    public final DateTimePath<java.util.Date> createTime = createDateTime("createTime", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QContentLikeRecord(String variable) {
        super(ContentLikeRecord.class, forVariable(variable));
    }

    public QContentLikeRecord(Path<? extends ContentLikeRecord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContentLikeRecord(PathMetadata metadata) {
        super(ContentLikeRecord.class, metadata);
    }

}


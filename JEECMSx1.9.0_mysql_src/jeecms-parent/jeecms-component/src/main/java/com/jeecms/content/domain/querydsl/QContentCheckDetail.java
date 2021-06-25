package com.jeecms.content.domain.querydsl;

import com.jeecms.content.domain.ContentCheckDetail;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QContentCheckDetail is a Querydsl query type for ContentCheckDetail
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QContentCheckDetail extends EntityPathBase<ContentCheckDetail> {

    private static final long serialVersionUID = 284075955L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContentCheckDetail contentCheckDetail = new QContentCheckDetail("contentCheckDetail");

    public final com.jeecms.common.base.domain.querydsl.QAbstractDomain _super = new com.jeecms.common.base.domain.querydsl.QAbstractDomain(this);

    public final StringPath checkBanContent = createString("checkBanContent");

    public final StringPath checkErrorContent = createString("checkErrorContent");

    public final StringPath checkMark = createString("checkMark");

    public final NumberPath<Integer> checkUserId = createNumber("checkUserId", Integer.class);

    public final QContent content;

    public final NumberPath<Integer> contentId = createNumber("contentId", Integer.class);

    //inherited
    public final DateTimePath<java.util.Date> createTime = _super.createTime;

    //inherited
    public final StringPath createUser = _super.createUser;

    public final NumberPath<Integer> fieldErrorNum = createNumber("fieldErrorNum", Integer.class);

    //inherited
    public final BooleanPath hasDeleted = _super.hasDeleted;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath pictureScene = createString("pictureScene");

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final StringPath textScene = createString("textScene");

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUser = _super.updateUser;

    public QContentCheckDetail(String variable) {
        this(ContentCheckDetail.class, forVariable(variable), INITS);
    }

    public QContentCheckDetail(Path<? extends ContentCheckDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContentCheckDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContentCheckDetail(PathMetadata metadata, PathInits inits) {
        this(ContentCheckDetail.class, metadata, inits);
    }

    public QContentCheckDetail(Class<? extends ContentCheckDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new QContent(forProperty("content"), inits.get("content")) : null;
    }

}


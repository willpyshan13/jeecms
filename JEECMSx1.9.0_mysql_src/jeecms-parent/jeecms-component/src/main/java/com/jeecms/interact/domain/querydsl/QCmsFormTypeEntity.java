package com.jeecms.interact.domain.querydsl;

import com.jeecms.interact.domain.CmsFormTypeEntity;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QCmsFormTypeEntity is a Querydsl query type for CmsFormTypeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCmsFormTypeEntity extends EntityPathBase<CmsFormTypeEntity> {

    private static final long serialVersionUID = 1907989846L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCmsFormTypeEntity cmsFormTypeEntity = new QCmsFormTypeEntity("cmsFormTypeEntity");

    public final com.jeecms.common.base.domain.querydsl.QAbstractSortDomain _super = new com.jeecms.common.base.domain.querydsl.QAbstractSortDomain(this);

    //inherited
    public final DateTimePath<java.util.Date> createTime = _super.createTime;

    //inherited
    public final StringPath createUser = _super.createUser;

    //inherited
    public final BooleanPath hasDeleted = _super.hasDeleted;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.jeecms.system.domain.querydsl.QCmsSite site;

    public final NumberPath<Integer> siteId = createNumber("siteId", Integer.class);

    //inherited
    public final NumberPath<Integer> sortNum = _super.sortNum;

    public final NumberPath<Integer> sortWeight = createNumber("sortWeight", Integer.class);

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUser = _super.updateUser;

    public QCmsFormTypeEntity(String variable) {
        this(CmsFormTypeEntity.class, forVariable(variable), INITS);
    }

    public QCmsFormTypeEntity(Path<? extends CmsFormTypeEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCmsFormTypeEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCmsFormTypeEntity(PathMetadata metadata, PathInits inits) {
        this(CmsFormTypeEntity.class, metadata, inits);
    }

    public QCmsFormTypeEntity(Class<? extends CmsFormTypeEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.site = inits.isInitialized("site") ? new com.jeecms.system.domain.querydsl.QCmsSite(forProperty("site"), inits.get("site")) : null;
    }

}


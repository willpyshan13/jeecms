package com.jeecms.interact.domain.querydsl;

import com.jeecms.interact.domain.CmsFormItemEntity;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QCmsFormItemEntity is a Querydsl query type for CmsFormItemEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCmsFormItemEntity extends EntityPathBase<CmsFormItemEntity> {

    private static final long serialVersionUID = 1036026479L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCmsFormItemEntity cmsFormItemEntity = new QCmsFormItemEntity("cmsFormItemEntity");

    public final com.jeecms.common.base.domain.querydsl.QAbstractDomain _super = new com.jeecms.common.base.domain.querydsl.QAbstractDomain(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.util.Date> createTime = _super.createTime;

    //inherited
    public final StringPath createUser = _super.createUser;

    public final StringPath dataType = createString("dataType");

    public final StringPath defValue = createString("defValue");

    public final StringPath field = createString("field");

    public final QCmsFormEntity form;

    public final NumberPath<Integer> formId = createNumber("formId", Integer.class);

    public final StringPath groupType = createString("groupType");

    //inherited
    public final BooleanPath hasDeleted = _super.hasDeleted;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isCustom = createBoolean("isCustom");

    public final BooleanPath isRequired = createBoolean("isRequired");

    public final StringPath itemLabel = createString("itemLabel");

    public final StringPath placeholder = createString("placeholder");

    public final NumberPath<Integer> sortNum = createNumber("sortNum", Integer.class);

    public final StringPath tipText = createString("tipText");

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUser = _super.updateUser;

    public QCmsFormItemEntity(String variable) {
        this(CmsFormItemEntity.class, forVariable(variable), INITS);
    }

    public QCmsFormItemEntity(Path<? extends CmsFormItemEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCmsFormItemEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCmsFormItemEntity(PathMetadata metadata, PathInits inits) {
        this(CmsFormItemEntity.class, metadata, inits);
    }

    public QCmsFormItemEntity(Class<? extends CmsFormItemEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.form = inits.isInitialized("form") ? new QCmsFormEntity(forProperty("form"), inits.get("form")) : null;
    }

}


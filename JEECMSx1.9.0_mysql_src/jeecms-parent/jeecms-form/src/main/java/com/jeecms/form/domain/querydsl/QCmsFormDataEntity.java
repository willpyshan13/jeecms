package com.jeecms.form.domain.querydsl;

import com.jeecms.form.domain.CmsFormDataEntity;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QCmsFormDataEntity is a Querydsl query type for CmsFormDataEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCmsFormDataEntity extends EntityPathBase<CmsFormDataEntity> {

    private static final long serialVersionUID = 1249906168L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCmsFormDataEntity cmsFormDataEntity = new QCmsFormDataEntity("cmsFormDataEntity");

    public final com.jeecms.common.base.domain.querydsl.QAbstractDomain _super = new com.jeecms.common.base.domain.querydsl.QAbstractDomain(this);

    public final ListPath<com.jeecms.form.domain.CmsFormDataAttrEntity, QCmsFormDataAttrEntity> attrs = this.<com.jeecms.form.domain.CmsFormDataAttrEntity, QCmsFormDataAttrEntity>createList("attrs", com.jeecms.form.domain.CmsFormDataAttrEntity.class, QCmsFormDataAttrEntity.class, PathInits.DIRECT2);

    public final com.jeecms.system.domain.querydsl.QArea city;

    public final StringPath cityCode = createString("cityCode");

    public final StringPath cookieIdentity = createString("cookieIdentity");

    //inherited
    public final DateTimePath<java.util.Date> createTime = _super.createTime;

    //inherited
    public final StringPath createUser = _super.createUser;

    public final com.jeecms.interact.domain.querydsl.QCmsFormEntity form;

    public final NumberPath<Integer> formId = createNumber("formId", Integer.class);

    //inherited
    public final BooleanPath hasDeleted = _super.hasDeleted;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ip = createString("ip");

    public final BooleanPath isPc = createBoolean("isPc");

    public final BooleanPath isRead = createBoolean("isRead");

    public final BooleanPath isRecycle = createBoolean("isRecycle");

    public final com.jeecms.system.domain.querydsl.QArea province;

    public final StringPath provinceCode = createString("provinceCode");

    public final StringPath systemInfo = createString("systemInfo");

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUser = _super.updateUser;

    public final com.jeecms.auth.domain.querydsl.QCoreUser user;

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final StringPath wxopenId = createString("wxopenId");

    public QCmsFormDataEntity(String variable) {
        this(CmsFormDataEntity.class, forVariable(variable), INITS);
    }

    public QCmsFormDataEntity(Path<? extends CmsFormDataEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCmsFormDataEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCmsFormDataEntity(PathMetadata metadata, PathInits inits) {
        this(CmsFormDataEntity.class, metadata, inits);
    }

    public QCmsFormDataEntity(Class<? extends CmsFormDataEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.city = inits.isInitialized("city") ? new com.jeecms.system.domain.querydsl.QArea(forProperty("city")) : null;
        this.form = inits.isInitialized("form") ? new com.jeecms.interact.domain.querydsl.QCmsFormEntity(forProperty("form"), inits.get("form")) : null;
        this.province = inits.isInitialized("province") ? new com.jeecms.system.domain.querydsl.QArea(forProperty("province")) : null;
        this.user = inits.isInitialized("user") ? new com.jeecms.auth.domain.querydsl.QCoreUser(forProperty("user"), inits.get("user")) : null;
    }

}


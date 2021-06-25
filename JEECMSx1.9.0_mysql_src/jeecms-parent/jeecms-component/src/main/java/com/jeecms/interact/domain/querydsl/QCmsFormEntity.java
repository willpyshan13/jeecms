package com.jeecms.interact.domain.querydsl;

import com.jeecms.interact.domain.CmsFormEntity;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QCmsFormEntity is a Querydsl query type for CmsFormEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCmsFormEntity extends EntityPathBase<CmsFormEntity> {

    private static final long serialVersionUID = -1043420676L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCmsFormEntity cmsFormEntity = new QCmsFormEntity("cmsFormEntity");

    public final com.jeecms.common.base.domain.querydsl.QAbstractDomain _super = new com.jeecms.common.base.domain.querydsl.QAbstractDomain(this);

    public final DateTimePath<java.util.Date> beginTime = createDateTime("beginTime", java.util.Date.class);

    public final StringPath bgConfig = createString("bgConfig");

    public final com.jeecms.resource.domain.querydsl.QResourcesSpaceData bgImg;

    public final NumberPath<Integer> bgImgId = createNumber("bgImgId", Integer.class);

    public final StringPath contConfig = createString("contConfig");

    public final com.jeecms.resource.domain.querydsl.QResourcesSpaceData coverPic;

    public final NumberPath<Integer> coverPicId = createNumber("coverPicId", Integer.class);

    //inherited
    public final DateTimePath<java.util.Date> createTime = _super.createTime;

    //inherited
    public final StringPath createUser = _super.createUser;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> deviceSubLimit = createNumber("deviceSubLimit", Integer.class);

    public final NumberPath<Short> deviceSubLimitUnit = createNumber("deviceSubLimitUnit", Short.class);

    public final DateTimePath<java.util.Date> endTime = createDateTime("endTime", java.util.Date.class);

    public final StringPath fontConfig = createString("fontConfig");

    public final NumberPath<Short> formScene = createNumber("formScene", Short.class);

    public final QCmsFormTypeEntity formType;

    //inherited
    public final BooleanPath hasDeleted = _super.hasDeleted;

    public final StringPath headConfig = createString("headConfig");

    public final com.jeecms.resource.domain.querydsl.QResourcesSpaceData headImg;

    public final NumberPath<Integer> headImgId = createNumber("headImgId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> ipSubLimit = createNumber("ipSubLimit", Integer.class);

    public final NumberPath<Short> ipSubLimitUnit = createNumber("ipSubLimitUnit", Short.class);

    public final BooleanPath isCaptcha = createBoolean("isCaptcha");

    public final BooleanPath isOnlyWechat = createBoolean("isOnlyWechat");

    public final ListPath<com.jeecms.interact.domain.CmsFormItemEntity, QCmsFormItemEntity> items = this.<com.jeecms.interact.domain.CmsFormItemEntity, QCmsFormItemEntity>createList("items", com.jeecms.interact.domain.CmsFormItemEntity.class, QCmsFormItemEntity.class, PathInits.DIRECT2);

    public final NumberPath<Integer> joinCount = createNumber("joinCount", Integer.class);

    public final NumberPath<Short> processType = createNumber("processType", Short.class);

    public final StringPath prompt = createString("prompt");

    public final StringPath shareDesc = createString("shareDesc");

    public final com.jeecms.resource.domain.querydsl.QResourcesSpaceData shareLogo;

    public final NumberPath<Integer> shareLogoId = createNumber("shareLogoId", Integer.class);

    public final com.jeecms.system.domain.querydsl.QCmsSite site;

    public final NumberPath<Integer> siteId = createNumber("siteId", Integer.class);

    public final NumberPath<Short> status = createNumber("status", Short.class);

    public final StringPath subConfig = createString("subConfig");

    public final BooleanPath submitLimitLogin = createBoolean("submitLimitLogin");

    public final StringPath title = createString("title");

    public final NumberPath<Integer> typeId = createNumber("typeId", Integer.class);

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUser = _super.updateUser;

    public final NumberPath<Integer> userSubLimit = createNumber("userSubLimit", Integer.class);

    public final NumberPath<Short> userSubLimitUnit = createNumber("userSubLimitUnit", Short.class);

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public final NumberPath<Integer> wechatSubLimit = createNumber("wechatSubLimit", Integer.class);

    public final NumberPath<Short> wechatSubLimitUnit = createNumber("wechatSubLimitUnit", Short.class);

    public QCmsFormEntity(String variable) {
        this(CmsFormEntity.class, forVariable(variable), INITS);
    }

    public QCmsFormEntity(Path<? extends CmsFormEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCmsFormEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCmsFormEntity(PathMetadata metadata, PathInits inits) {
        this(CmsFormEntity.class, metadata, inits);
    }

    public QCmsFormEntity(Class<? extends CmsFormEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bgImg = inits.isInitialized("bgImg") ? new com.jeecms.resource.domain.querydsl.QResourcesSpaceData(forProperty("bgImg"), inits.get("bgImg")) : null;
        this.coverPic = inits.isInitialized("coverPic") ? new com.jeecms.resource.domain.querydsl.QResourcesSpaceData(forProperty("coverPic"), inits.get("coverPic")) : null;
        this.formType = inits.isInitialized("formType") ? new QCmsFormTypeEntity(forProperty("formType"), inits.get("formType")) : null;
        this.headImg = inits.isInitialized("headImg") ? new com.jeecms.resource.domain.querydsl.QResourcesSpaceData(forProperty("headImg"), inits.get("headImg")) : null;
        this.shareLogo = inits.isInitialized("shareLogo") ? new com.jeecms.resource.domain.querydsl.QResourcesSpaceData(forProperty("shareLogo"), inits.get("shareLogo")) : null;
        this.site = inits.isInitialized("site") ? new com.jeecms.system.domain.querydsl.QCmsSite(forProperty("site"), inits.get("site")) : null;
    }

}


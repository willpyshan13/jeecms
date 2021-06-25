package com.jeecms.form.domain.querydsl;

import com.jeecms.form.domain.CmsFormDataAttrEntity;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QCmsFormDataAttrEntity is a Querydsl query type for CmsFormDataAttrEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCmsFormDataAttrEntity extends EntityPathBase<CmsFormDataAttrEntity> {

    private static final long serialVersionUID = 55363785L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCmsFormDataAttrEntity cmsFormDataAttrEntity = new QCmsFormDataAttrEntity("cmsFormDataAttrEntity");

    public final com.jeecms.system.domain.querydsl.QArea area;

    public final StringPath areaCode = createString("areaCode");

    public final StringPath attrName = createString("attrName");

    public final ListPath<com.jeecms.form.domain.CmsFormDataAttrResEntity, QCmsFormDataAttrResEntity> attrRes = this.<com.jeecms.form.domain.CmsFormDataAttrResEntity, QCmsFormDataAttrResEntity>createList("attrRes", com.jeecms.form.domain.CmsFormDataAttrResEntity.class, QCmsFormDataAttrResEntity.class, PathInits.DIRECT2);

    public final NumberPath<Short> attrType = createNumber("attrType", Short.class);

    public final StringPath attrValue = createString("attrValue");

    public final com.jeecms.system.domain.querydsl.QArea city;

    public final StringPath cityCode = createString("cityCode");

    public final QCmsFormDataEntity data;

    public final NumberPath<Integer> dataId = createNumber("dataId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath pdfPath = createString("pdfPath");

    public final com.jeecms.system.domain.querydsl.QArea province;

    public final StringPath provinceCode = createString("provinceCode");

    public final NumberPath<Integer> resId = createNumber("resId", Integer.class);

    public final com.jeecms.resource.domain.querydsl.QResourcesSpaceData resourcesSpaceData;

    public QCmsFormDataAttrEntity(String variable) {
        this(CmsFormDataAttrEntity.class, forVariable(variable), INITS);
    }

    public QCmsFormDataAttrEntity(Path<? extends CmsFormDataAttrEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCmsFormDataAttrEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCmsFormDataAttrEntity(PathMetadata metadata, PathInits inits) {
        this(CmsFormDataAttrEntity.class, metadata, inits);
    }

    public QCmsFormDataAttrEntity(Class<? extends CmsFormDataAttrEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.area = inits.isInitialized("area") ? new com.jeecms.system.domain.querydsl.QArea(forProperty("area")) : null;
        this.city = inits.isInitialized("city") ? new com.jeecms.system.domain.querydsl.QArea(forProperty("city")) : null;
        this.data = inits.isInitialized("data") ? new QCmsFormDataEntity(forProperty("data"), inits.get("data")) : null;
        this.province = inits.isInitialized("province") ? new com.jeecms.system.domain.querydsl.QArea(forProperty("province")) : null;
        this.resourcesSpaceData = inits.isInitialized("resourcesSpaceData") ? new com.jeecms.resource.domain.querydsl.QResourcesSpaceData(forProperty("resourcesSpaceData"), inits.get("resourcesSpaceData")) : null;
    }

}


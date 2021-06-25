package com.jeecms.form.domain.querydsl;

import com.jeecms.form.domain.CmsFormDataAttrResEntity;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathInits;
import com.querydsl.core.types.dsl.StringPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QCmsFormDataAttrResEntity is a Querydsl query type for CmsFormDataAttrResEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCmsFormDataAttrResEntity extends EntityPathBase<CmsFormDataAttrResEntity> {

    private static final long serialVersionUID = 1918810621L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCmsFormDataAttrResEntity cmsFormDataAttrResEntity = new QCmsFormDataAttrResEntity("cmsFormDataAttrResEntity");

    public final QCmsFormDataAttrEntity attr;

    public final NumberPath<Integer> attrId = createNumber("attrId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath pdfPath = createString("pdfPath");

    public final StringPath resDesc = createString("resDesc");

    public final NumberPath<Integer> resId = createNumber("resId", Integer.class);

    public final com.jeecms.resource.domain.querydsl.QResourcesSpaceData resourcesSpaceData;

    public QCmsFormDataAttrResEntity(String variable) {
        this(CmsFormDataAttrResEntity.class, forVariable(variable), INITS);
    }

    public QCmsFormDataAttrResEntity(Path<? extends CmsFormDataAttrResEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCmsFormDataAttrResEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCmsFormDataAttrResEntity(PathMetadata metadata, PathInits inits) {
        this(CmsFormDataAttrResEntity.class, metadata, inits);
    }

    public QCmsFormDataAttrResEntity(Class<? extends CmsFormDataAttrResEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.attr = inits.isInitialized("attr") ? new QCmsFormDataAttrEntity(forProperty("attr"), inits.get("attr")) : null;
        this.resourcesSpaceData = inits.isInitialized("resourcesSpaceData") ? new com.jeecms.resource.domain.querydsl.QResourcesSpaceData(forProperty("resourcesSpaceData"), inits.get("resourcesSpaceData")) : null;
    }

}


package com.jeecms.auth.domain.querydsl;

import com.jeecms.auth.domain.UserModelSort;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QUserModelSort is a Querydsl query type for UserModelSort
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUserModelSort extends EntityPathBase<UserModelSort> {

    private static final long serialVersionUID = 1219673526L;

    public static final QUserModelSort userModelSort = new QUserModelSort("userModelSort");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> modelId = createNumber("modelId", Integer.class);

    public final NumberPath<Integer> sort = createNumber("sort", Integer.class);

    public final DatePath<java.util.Date> statisticsDay = createDate("statisticsDay", java.util.Date.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QUserModelSort(String variable) {
        super(UserModelSort.class, forVariable(variable));
    }

    public QUserModelSort(Path<? extends UserModelSort> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserModelSort(PathMetadata metadata) {
        super(UserModelSort.class, metadata);
    }

}


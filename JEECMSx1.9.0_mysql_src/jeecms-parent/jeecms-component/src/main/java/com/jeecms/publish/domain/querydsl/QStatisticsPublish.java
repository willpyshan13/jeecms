package com.jeecms.publish.domain.querydsl;

import com.jeecms.publish.domain.StatisticsPublish;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QStatisticsPublish is a Querydsl query type for StatisticsPublish
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStatisticsPublish extends EntityPathBase<StatisticsPublish> {

    private static final long serialVersionUID = -706596413L;

    public static final QStatisticsPublish statisticsPublish = new QStatisticsPublish("statisticsPublish");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> siteId = createNumber("siteId", Integer.class);

    public final NumberPath<Integer> numbers = createNumber("numbers", Integer.class);

    public final DateTimePath<java.util.Date> statisticsDay = createDateTime("statisticsDay", java.util.Date.class);

    public final NumberPath<Integer> types = createNumber("types", Integer.class);

    public QStatisticsPublish(String variable) {
        super(StatisticsPublish.class, forVariable(variable));
    }

    public QStatisticsPublish(Path<? extends StatisticsPublish> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatisticsPublish(PathMetadata metadata) {
        super(StatisticsPublish.class, metadata);
    }

}


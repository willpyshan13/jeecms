package com.jeecms.auth.domain.querydsl;

import com.jeecms.auth.domain.UserIncome;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QUserIncome is a Querydsl query type for UserIncome
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUserIncome extends EntityPathBase<UserIncome> {

    private static final long serialVersionUID = 1612457658L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserIncome userIncome = new QUserIncome("userIncome");

    public final com.jeecms.common.base.domain.querydsl.QAbstractDomain _super = new com.jeecms.common.base.domain.querydsl.QAbstractDomain(this);

    public final NumberPath<Long> balance = createNumber("balance", Long.class);

    //inherited
    public final DateTimePath<java.util.Date> createTime = _super.createTime;

    //inherited
    public final StringPath createUser = _super.createUser;

    //inherited
    public final BooleanPath hasDeleted = _super.hasDeleted;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Long> paidAmount = createNumber("paidAmount", Long.class);

    public final NumberPath<Long> rewardAmount = createNumber("rewardAmount", Long.class);

    public final NumberPath<Long> totalAmount = createNumber("totalAmount", Long.class);

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUser = _super.updateUser;

    public final QCoreUser user;

    public final NumberPath<Long> waitingSettlementAmount = createNumber("waitingSettlementAmount", Long.class);

    public final NumberPath<Long> withdrawalAmount = createNumber("withdrawalAmount", Long.class);

    public QUserIncome(String variable) {
        this(UserIncome.class, forVariable(variable), INITS);
    }

    public QUserIncome(Path<? extends UserIncome> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserIncome(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserIncome(PathMetadata metadata, PathInits inits) {
        this(UserIncome.class, metadata, inits);
    }

    public QUserIncome(Class<? extends UserIncome> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QCoreUser(forProperty("user"), inits.get("user")) : null;
    }

}


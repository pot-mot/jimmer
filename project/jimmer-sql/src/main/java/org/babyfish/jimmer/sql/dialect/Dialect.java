package org.babyfish.jimmer.sql.dialect;

import org.babyfish.jimmer.sql.ast.impl.Ast;
import org.babyfish.jimmer.sql.ast.impl.render.AbstractSqlBuilder;
import org.babyfish.jimmer.sql.ast.impl.value.ValueGetter;
import org.babyfish.jimmer.sql.meta.SqlTypeStrategy;
import org.babyfish.jimmer.sql.exception.ExecutionException;
import org.babyfish.jimmer.sql.runtime.Reader;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public interface Dialect extends SqlTypeStrategy {

    String FAKE_UPDATE_COMMENT = "/* fake update to return all ids */";

    void paginate(PaginationContext ctx);

    @Nullable
    default UpdateJoin getUpdateJoin() {
        return null;
    }

    default String getSelectIdFromSequenceSql(String sequenceName) {
        throw new ExecutionException("Sequence is not supported by '" + getClass().getName() + "'");
    }

    @Nullable
    default String getOverrideIdentityIdSql() {
        return null;
    }

    default boolean isDeletedAliasRequired() { return false; }

    default boolean isDeleteAliasSupported() { return true; }

    default boolean isUpdateAliasSupported() { return true; }

    @Nullable
    default String getOffsetOptimizationNumField() {
        return null;
    }

    default boolean isMultiInsertionSupported() { return true; }

    default boolean isArraySupported() { return false; }

    default boolean isAnyEqualityOfArraySupported() {
        return isArraySupported();
    }

    default <T> T[] getArray(ResultSet rs, int col, Class<T[]> arrayType) throws SQLException {
        throw new UnsupportedOperationException("`Dialect.getArray` is not supported");
    }

    default boolean isTupleSupported() {
        return true;
    }

    default boolean isTupleComparisonSupported() {
        return isTupleSupported();
    }

    default boolean isTupleCountSupported() {
        return false;
    }

    default boolean isTableOfSubQueryMutable() {
        return true;
    }

    @Nullable
    default String getConstantTableName() { return null; }

    default Class<?> getJsonBaseType() {
        return String.class;
    }

    @Nullable
    default Object jsonToBaseValue(@Nullable String json) throws SQLException {
        return json;
    }

    @Nullable
    default String baseValueToJson(@Nullable Object baseValue) throws SQLException {
        return (String) baseValue;
    }

    default boolean isForeignKeySupported() {
        return true;
    }

    default boolean isIgnoreCaseLikeSupported() { return false; }

    default int resolveJdbcType(Class<?> sqlType) {
        return Types.OTHER;
    }

    default Reader<?> unknownReader(Class<?> sqlType) {
        return null;
    }

    default String transCacheOperatorTableDDL() {
        throw new UnsupportedOperationException(
                "The current dialect \"" +
                        getClass().getName() +
                        "\" does not know how to create table `JIMMER_TRANS_CACHE_OPERATOR`"
        );
    }

    default int getMaxInListSize() {
        return 1000;
    }

    @Override
    default String arrayTypeSuffix() {
        return "[]";
    }

    default boolean isIdFetchableByKeyUpdate() {
        return false;
    }

    default boolean isInsertedIdReturningRequired() {
        return false;
    }

    default boolean isExplicitBatchRequired() {
        return false;
    }

    /**
     * Can batch operation returns
     * generated Ids and affected row counts
     */
    default boolean isBatchDumb() {
        return false;
    }

    default boolean isUpsertSupported() {
        return false;
    }

    default boolean isNoIdUpsertSupported() {
        return isUpsertSupported();
    }

    default boolean isUpsertWithOptimisticLockSupported() {
        return false;
    }

    default boolean isUpsertWithMultipleUniqueConstraintSupported() {
        return true;
    }

    default boolean isUpsertWithNullableKeySupported() {
        return false;
    }

    default boolean isTransactionAbortedByError() {
        return false;
    }

    default boolean isBatchUpdateExceptionUnreliable() {
        return false;
    }

    void update(UpdateContext ctx);

    void upsert(UpsertContext ctx);

    interface UpdateContext {

        boolean isUpdatedByKey();

        UpdateContext sql(String sql);
        UpdateContext sql(ValueGetter getter);
        UpdateContext enter(AbstractSqlBuilder.ScopeType type);
        UpdateContext separator();
        UpdateContext leave();

        UpdateContext appendTableName();
        UpdateContext appendAssignments();
        UpdateContext appendPredicates();
        UpdateContext appendId();
    }

    interface UpsertContext {

        boolean hasUpdatedColumns();
        boolean hasOptimisticLock();
        boolean hasGeneratedId();
        boolean isUpdateIgnored();
        boolean isComplete();
        List<ValueGetter> getConflictGetters();

        UpsertContext sql(String sql);
        UpsertContext sql(ValueGetter getter);
        UpsertContext enter(AbstractSqlBuilder.ScopeType type);
        UpsertContext separator();
        UpsertContext leave();

        UpsertContext appendTableName();
        UpsertContext appendInsertedColumns(String prefix);
        UpsertContext appendConflictColumns();
        UpsertContext appendInsertingValues();
        UpsertContext appendUpdatingAssignments(String prefix, String suffix);
        UpsertContext appendOptimisticLockCondition(String sourceTablePrefix);
        UpsertContext appendGeneratedId();
    }

    default void renderLPad(
            AbstractSqlBuilder<?> builder,
            int currentPrecedence,
            Ast expression,
            Ast length,
            Ast padString
    ) {
        builder.sql("lpad(")
                .ast(expression, currentPrecedence)
                .sql(", ")
                .ast(length, currentPrecedence)
                .sql(", ")
                .ast(padString, currentPrecedence)
                .sql(")");
    }

    default void renderRPad(
            AbstractSqlBuilder<?> builder,
            int currentPrecedence,
            Ast expression,
            Ast length,
            Ast padString
    ) {
        builder.sql("rpad(")
                .ast(expression, currentPrecedence)
                .sql(", ")
                .ast(length, currentPrecedence)
                .sql(", ")
                .ast(padString, currentPrecedence)
                .sql(")");
    }

    default void renderPosition(
            AbstractSqlBuilder<?> builder,
            int currentPrecedence,
            Ast subStrAst,
            Ast expressionAst,
            @Nullable Ast startAst
    ) {
        if (startAst != null) {
            throw new IllegalArgumentException(
                    "The dialect \"" +
                            getClass().getName() +
                            "\" does not support the third `start` parameter of `position`"
            );
        }
        builder.sql("position(")
                    .ast(expressionAst, currentPrecedence)
                    .sql(" in ")
                    .ast(subStrAst, currentPrecedence)
                    .sql(")");
    }

    default void renderLeft(
            AbstractSqlBuilder<?> builder,
            int currentPrecedence,
            Ast expressionAst,
            Ast lengthAst
    ) {
        builder.sql("left(")
                .ast(expressionAst, currentPrecedence)
                .sql(", ")
                .ast(lengthAst, currentPrecedence)
                .sql(")");
    }

    default void renderRight(
            AbstractSqlBuilder<?> builder,
            int currentPrecedence,
            Ast expressionAst,
            Ast lengthAst
    ) {
        builder.sql("right(")
                .ast(expressionAst, currentPrecedence)
                .sql(", ")
                .ast(lengthAst, currentPrecedence)
                .sql(")");
    }

    default void renderSubString(
            AbstractSqlBuilder<?> builder,
            int currentPrecedence,
            Ast expressionAst,
            Ast startAst,
            @Nullable Ast lengthAst
    ) {
        builder.sql("substring(")
                .ast(expressionAst, currentPrecedence)
                .sql(", ")
                .ast(startAst, currentPrecedence);
        if (lengthAst != null) {
            builder.sql(", ")
                    .ast(lengthAst, currentPrecedence);
        }
        builder.sql(")");
    }
}

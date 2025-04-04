package org.babyfish.jimmer.sql.kt.query

import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.table.isNull
import org.babyfish.jimmer.sql.kt.common.AbstractQueryTest
import org.babyfish.jimmer.sql.kt.model.TreeNode
import org.babyfish.jimmer.sql.kt.model.classic.author.Author
import org.babyfish.jimmer.sql.kt.model.classic.author.fetchBy
import org.babyfish.jimmer.sql.kt.model.classic.author.firstName
import org.babyfish.jimmer.sql.kt.model.classic.book.Book
import org.babyfish.jimmer.sql.kt.model.classic.book.fetchBy
import org.babyfish.jimmer.sql.kt.model.classic.book.id
import org.babyfish.jimmer.sql.kt.model.classic.store.BookStore
import org.babyfish.jimmer.sql.kt.model.classic.store.fetchBy
import org.babyfish.jimmer.sql.kt.model.embedded.Dependency
import org.babyfish.jimmer.sql.kt.model.embedded.dto.DependencyView
import org.babyfish.jimmer.sql.kt.model.fetchBy
import org.babyfish.jimmer.sql.kt.model.`parent?`
import org.junit.Test

class FetcherTest : AbstractQueryTest() {

    @Test
    fun testWithoutFilter() {
        executeAndExpect(
            sqlClient.createQuery(Book::class) {
                where(table.id eq 1L)
                select(
                    table.fetchBy {
                        allScalarFields()
                        store {
                            allScalarFields()
                        }
                        authors {
                            allScalarFields()
                        }
                    }
                )
            }
        ) {
            sql(
                """select tb_1_.ID, tb_1_.NAME, tb_1_.EDITION, tb_1_.PRICE, tb_1_.STORE_ID 
                    |from BOOK tb_1_ 
                    |where tb_1_.ID = ?""".trimMargin()
            )
            variables(1L)
            statement(1).apply {
                sql(
                    """select tb_1_.ID, tb_1_.NAME, tb_1_.VERSION, tb_1_.WEBSITE 
                        |from BOOK_STORE tb_1_ 
                        |where tb_1_.ID = ?""".trimMargin()
                )
                variables(1L)
            }
            statement(2).apply {
                sql(
                    """select tb_1_.ID, tb_1_.FIRST_NAME, tb_1_.LAST_NAME, tb_1_.GENDER 
                        |from AUTHOR tb_1_ 
                        |inner join BOOK_AUTHOR_MAPPING tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
                        |where tb_2_.BOOK_ID = ?""".trimMargin()
                )
                variables(1L)
            }
            rows(
                """[
                    |--->{
                    |--->--->"id":1,
                    |--->--->"name":"Learning GraphQL",
                    |--->--->"edition":1,
                    |--->--->"price":50.00,
                    |--->--->"store":{
                    |--->--->--->"id":1,
                    |--->--->--->"name":"O'REILLY",
                    |--->--->--->"version":0,
                    |--->--->--->"website":null
                    |--->--->},
                    |--->--->"authors":[
                    |--->--->--->{
                    |--->--->--->--->"id":1,
                    |--->--->--->--->"firstName":"Eve",
                    |--->--->--->--->"lastName":"Procello",
                    |--->--->--->--->"gender":"FEMALE"
                    |--->--->--->},{
                    |--->--->--->--->"id":2,
                    |--->--->--->--->"firstName":"Alex",
                    |--->--->--->--->"lastName":"Banks",
                    |--->--->--->--->"gender":"MALE"
                    |--->--->--->}
                    |--->--->]
                    |--->}
                    |]""".trimMargin()
            )
        }
    }

    @Test
    fun testWithFilter() {
        executeAndExpect(
            sqlClient.createQuery(Book::class) {
                where(table.id eq 1L)
                select(
                    table.fetchBy {
                        allScalarFields()
                        store {
                            allScalarFields()
                        }
                        authors {
                            allScalarFields()
                        }
                    },
                    table.fetchBy {
                        allScalarFields()
                        store {
                            allScalarFields()
                        }
                        authors({
                            filter {
                                where(table.firstName eq "Alex")
                            }
                        }) {
                            allScalarFields()
                        }
                    }
                )
            }
        ) {
            sql(
                """select 
                    |tb_1_.ID, tb_1_.NAME, tb_1_.EDITION, tb_1_.PRICE, tb_1_.STORE_ID, 
                    |tb_1_.ID, tb_1_.NAME, tb_1_.EDITION, tb_1_.PRICE, tb_1_.STORE_ID 
                    |from BOOK tb_1_ 
                    |where tb_1_.ID = ?""".trimMargin()
            )
            variables(1L)
            statement(1).apply {
                sql(
                    """select 
                        |tb_1_.ID, tb_1_.NAME, tb_1_.VERSION, tb_1_.WEBSITE 
                        |from BOOK_STORE tb_1_ 
                        |where tb_1_.ID = ?""".trimMargin()
                )
                variables(1L)
            }
            statement(2).apply {
                sql(
                    """select 
                        |tb_1_.ID, tb_1_.FIRST_NAME, tb_1_.LAST_NAME, tb_1_.GENDER 
                        |from AUTHOR tb_1_ 
                        |inner join BOOK_AUTHOR_MAPPING tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
                        |where tb_2_.BOOK_ID = ?""".trimMargin()
                )
                variables(1L)
            }
            statement(3).apply {
                sql(
                    """select 
                        |tb_1_.ID, tb_1_.NAME, tb_1_.VERSION, tb_1_.WEBSITE 
                        |from BOOK_STORE tb_1_ 
                        |where tb_1_.ID = ?""".trimMargin()
                )
                variables(1L)
            }
            statement(4).apply {
                sql(
                    """select tb_1_.ID, tb_1_.FIRST_NAME, tb_1_.LAST_NAME, tb_1_.GENDER 
                        |from AUTHOR tb_1_ 
                        |inner join BOOK_AUTHOR_MAPPING tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
                        |where 
                        |--->tb_2_.BOOK_ID = ? 
                        |and 
                        |--->tb_1_.FIRST_NAME = ?""".trimMargin()
                )
                variables(1L, "Alex")
            }
            rows {
                contentEquals(
                    """[
                        |--->Tuple2(
                        |--->--->_1={
                        |--->--->--->"id":1,
                        |--->--->--->"name":"Learning GraphQL",
                        |--->--->--->"edition":1,
                        |--->--->--->"price":50.00,
                        |--->--->--->"store":{
                        |--->--->--->--->"id":1,
                        |--->--->--->--->"name":"O'REILLY",
                        |--->--->--->--->"version":0,
                        |--->--->--->--->"website":null
                        |--->--->--->},
                        |--->--->--->"authors":[
                        |--->--->--->--->{
                        |--->--->--->--->--->"id":1,
                        |--->--->--->--->--->"firstName":"Eve",
                        |--->--->--->--->--->"lastName":"Procello",
                        |--->--->--->--->--->"gender":"FEMALE"
                        |--->--->--->--->},{
                        |--->--->--->--->--->"id":2,
                        |--->--->--->--->--->"firstName":"Alex",
                        |--->--->--->--->--->"lastName":"Banks",
                        |--->--->--->--->--->"gender":"MALE"
                        |--->--->--->--->}
                        |--->--->--->]
                        |--->--->}, 
                        |--->--->_2={
                        |--->--->--->"id":1,
                        |--->--->--->"name":"Learning GraphQL",
                        |--->--->--->"edition":1,
                        |--->--->--->"price":50.00,
                        |--->--->--->"store":{
                        |--->--->--->--->"id":1,
                        |--->--->--->--->"name":"O'REILLY",
                        |--->--->--->--->"version":0,
                        |--->--->--->--->"website":null
                        |--->--->--->},
                        |--->--->--->"authors":[
                        |--->--->--->--->{
                        |--->--->--->--->--->"id":2,
                        |--->--->--->--->--->"firstName":"Alex",
                        |--->--->--->--->--->"lastName":"Banks",
                        |--->--->--->--->--->"gender":"MALE"
                        |--->--->--->--->}
                        |--->--->--->]
                        |--->--->}
                        |--->)
                        |]""".trimMargin(),
                    it.toString()
                )
            }
        }
    }

    @Test
    fun testFetchTwoLayers() {
        executeAndExpect(
            sqlClient.createQuery(TreeNode::class) {
                where(table.`parent?`.isNull())
                select(
                    table.fetchBy {
                        allScalarFields()
                        `childNodes*`{
                            depth(2)
                        }
                    }
                )
            }
        ) {
            sql(
                """select tb_1_.NODE_ID, tb_1_.NAME 
                    |from TREE_NODE tb_1_ 
                    |where tb_1_.PARENT_ID is null""".trimMargin()
            )
            statement(1).apply {
                sql(
                    """select tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID = ? 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(1L)
            }
            statement(2).apply {
                sql(
                    """select 
                        |--->tb_1_.PARENT_ID, 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID in (?, ?) 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(2L, 9L))
            }
            rows(
                """[
                    |--->{
                    |--->--->"id":1,
                    |--->--->"name":"Home",
                    |--->--->"childNodes":[
                    |--->--->--->{
                    |--->--->--->--->"id":2,
                    |--->--->--->--->"name":"Food",
                    |--->--->--->--->"childNodes":[
                    |--->--->--->--->--->{"id":3,"name":"Drinks"},
                    |--->--->--->--->--->{"id":6,"name":"Bread"}
                    |--->--->--->--->]
                    |--->--->--->},{
                    |--->--->--->--->"id":9,
                    |--->--->--->--->"name":"Clothing",
                    |--->--->--->--->"childNodes":[
                    |--->--->--->--->--->{"id":10,"name":"Woman"},
                    |--->--->--->--->--->{"id":18,"name":"Man"}
                    |--->--->--->--->]
                    |--->--->--->}
                    |--->--->]
                    |--->}
                    |]""".trimMargin()
            )
        }
    }

    @Test
    fun testRecursive() {
        executeAndExpect(
            sqlClient.createQuery(TreeNode::class) {
                where(table.`parent?`.isNull())
                select(
                    table.fetchBy {
                        allScalarFields()
                        `childNodes*`()
                    }
                )
            }
        ) {
            sql(
                """select tb_1_.NODE_ID, tb_1_.NAME 
                    |from TREE_NODE tb_1_ 
                    |where tb_1_.PARENT_ID is null""".trimMargin()
            )
            statement(1).apply {
                sql(
                    """select tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID = ? 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(1L)
            }
            statement(2).apply {
                sql(
                    """select 
                        |--->tb_1_.PARENT_ID, 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID in (?, ?) 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(2L, 9L))
            }
            statement(3).apply {
                sql(
                    """select 
                        |--->tb_1_.PARENT_ID, 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID in (?, ?, ?, ?) 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(3L, 6L, 10L, 18L))
            }
            statement(4).apply {
                sql(
                    """select 
                        |--->tb_1_.PARENT_ID, 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID in (?, ?, ?, ?, ?, ?, ?, ?) 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(4L, 5L, 7L, 8L, 11L, 15L, 19L, 22L))
            }
            statement(5).apply {
                sql(
                    """select 
                        |--->tb_1_.PARENT_ID, 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID in (?, ?, ?, ?, ?, ?, ?, ?, ?) 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(12L, 13L, 14L, 16L, 17L, 20L, 21L, 23, 24L))
            }
            rows(
                "[{" +
                    "--->\"id\":1," +
                    "--->\"name\":" +
                    "--->\"Home\"," +
                    "--->\"childNodes\":[" +
                    "--->--->{" +
                    "--->--->--->\"id\":2," +
                    "--->--->--->\"name\":\"Food\"," +
                    "--->--->--->\"childNodes\":[" +
                    "--->--->--->--->{" +
                    "--->--->--->--->--->\"id\":3," +
                    "--->--->--->--->--->\"name\":\"Drinks\"," +
                    "--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->{\"id\":4,\"name\":\"Coca Cola\",\"childNodes\":[]}," +
                    "--->--->--->--->--->--->{\"id\":5,\"name\":\"Fanta\",\"childNodes\":[]}" +
                    "--->--->--->--->--->]" +
                    "--->--->--->--->}," +
                    "--->--->--->--->{" +
                    "--->--->--->--->--->\"id\":6," +
                    "--->--->--->--->--->\"name\":\"Bread\"," +
                    "--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->{\"id\":7,\"name\":\"Baguette\",\"childNodes\":[]}," +
                    "--->--->--->--->--->--->{\"id\":8,\"name\":\"Ciabatta\",\"childNodes\":[]}" +
                    "--->--->--->--->--->]" +
                    "--->--->--->--->}" +
                    "--->--->--->]" +
                    "--->--->},{" +
                    "--->--->--->\"id\":9," +
                    "--->--->--->\"name\":\"Clothing\"," +
                    "--->--->--->\"childNodes\":[" +
                    "--->--->--->--->{" +
                    "--->--->--->--->--->\"id\":10," +
                    "--->--->--->--->--->\"name\":\"Woman\"," +
                    "--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->{" +
                    "--->--->--->--->--->--->--->\"id\":11," +
                    "--->--->--->--->--->--->--->\"name\":\"Casual wear\"," +
                    "--->--->--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->--->--->{\"id\":12,\"name\":\"Dress\",\"childNodes\":[]}," +
                    "--->--->--->--->--->--->--->--->{\"id\":13,\"name\":\"Miniskirt\",\"childNodes\":[]}," +
                    "--->--->--->--->--->--->--->--->{\"id\":14,\"name\":\"Jeans\",\"childNodes\":[]}" +
                    "--->--->--->--->--->--->--->]" +
                    "--->--->--->--->--->--->},{" +
                    "--->--->--->--->--->--->--->\"id\":15," +
                    "--->--->--->--->--->--->--->\"name\":\"Formal wear\"," +
                    "--->--->--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->--->--->{\"id\":16,\"name\":\"Suit\",\"childNodes\":[]}," +
                    "--->--->--->--->--->--->--->--->{\"id\":17,\"name\":\"Shirt\",\"childNodes\":[]}" +
                    "--->--->--->--->--->--->--->]" +
                    "--->--->--->--->--->--->}" +
                    "--->--->--->--->--->]" +
                    "--->--->--->--->},{" +
                    "--->--->--->--->--->\"id\":18," +
                    "--->--->--->--->--->\"name\":\"Man\"," +
                    "--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->{" +
                    "--->--->--->--->--->--->--->\"id\":19," +
                    "--->--->--->--->--->--->--->\"name\":\"Casual wear\"," +
                    "--->--->--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->--->--->{\"id\":20,\"name\":\"Jacket\",\"childNodes\":[]}," +
                    "--->--->--->--->--->--->--->--->{\"id\":21,\"name\":\"Jeans\",\"childNodes\":[]}" +
                    "--->--->--->--->--->--->--->]" +
                    "--->--->--->--->--->--->},{" +
                    "--->--->--->--->--->--->--->\"id\":22," +
                    "--->--->--->--->--->--->--->\"name\":\"Formal wear\"," +
                    "--->--->--->--->--->--->--->\"childNodes\":[" +
                    "--->--->--->--->--->--->--->--->{\"id\":23,\"name\":\"Suit\",\"childNodes\":[]}," +
                    "--->--->--->--->--->--->--->--->{\"id\":24,\"name\":\"Shirt\",\"childNodes\":[]}" +
                    "--->--->--->--->--->--->--->]" +
                    "--->--->--->--->--->--->}" +
                    "--->--->--->--->--->]" +
                    "--->--->--->--->}" +
                    "--->--->--->]" +
                    "--->--->}" +
                    "--->]" +
                    "}]"
            )
        }
    }

    @Test
    fun testRecursiveExceptClothing() {
        executeAndExpect(
            sqlClient.createQuery(TreeNode::class) {
                where(table.`parent?`.isNull())
                select(
                    table.fetchBy {
                        allScalarFields()
                        `childNodes*` {
                            recursive {
                                entity.name != "Clothing"
                            }
                        }
                    }
                )
            }
        ) {
            sql(
                """select tb_1_.NODE_ID, tb_1_.NAME 
                    |from TREE_NODE tb_1_ 
                    |where tb_1_.PARENT_ID is null""".trimMargin()
            )
            statement(1).apply {
                sql(
                    """select tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID = ? 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(1L)
            }
            statement(2).apply {
                sql(
                    """select 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID = ? 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(2L))
            }
            statement(3).apply {
                sql(
                    """select 
                        |--->tb_1_.PARENT_ID, 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID in (?, ?) 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(3L, 6L))
            }
            statement(4).apply {
                sql(
                    """select 
                        |--->tb_1_.PARENT_ID, 
                        |--->tb_1_.NODE_ID, tb_1_.NAME 
                        |from TREE_NODE tb_1_ 
                        |where tb_1_.PARENT_ID in (?, ?, ?, ?) 
                        |order by tb_1_.NODE_ID asc""".trimMargin()
                )
                variables(setOf(4L, 5L, 7L, 8L))
            }
            rows(
                """[
                    |{
                    |--->"id":1,
                    |--->"name":"Home",
                    |--->"childNodes":[
                    |--->--->{
                    |--->--->--->"id":2,
                    |--->--->--->"name":"Food",
                    |--->--->--->"childNodes":[
                    |--->--->--->--->{
                    |--->--->--->--->--->"id":3,
                    |--->--->--->--->--->"name":"Drinks",
                    |--->--->--->--->--->"childNodes":[
                    |--->--->--->--->--->--->{"id":4,"name":"Coca Cola","childNodes":[]
                    |--->--->--->--->--->--->},{"id":5,"name":"Fanta","childNodes":[]}
                    |--->--->--->--->--->]
                    |--->--->--->--->},
                    |--->--->--->--->{
                    |--->--->--->--->--->"id":6,"name":"Bread",
                    |--->--->--->--->--->"childNodes":[
                    |--->--->--->--->--->--->{"id":7,"name":"Baguette","childNodes":[]},
                    |--->--->--->--->--->--->{"id":8,"name":"Ciabatta","childNodes":[]}
                    |--->--->--->--->--->]
                    |--->--->--->--->}
                    |--->--->--->]
                    |--->--->},
                    |--->--->{"id":9,"name":"Clothing"}
                    |--->]
                    |}
                    |]""".trimMargin()
            )
        }
    }

    @Test
    fun testCalculation() {
        executeAndExpect(
            sqlClient.createQuery(BookStore::class) {
                select(table.fetchBy {
                    allScalarFields()
                    avgPrice()
                })
            }
        ) {
            sql(
                """select tb_1_.ID, tb_1_.NAME, tb_1_.VERSION, tb_1_.WEBSITE from BOOK_STORE tb_1_"""
            )
            statement(1).sql(
                """select tb_1_.STORE_ID, coalesce(avg(tb_1_.PRICE), ?) 
                    |from BOOK tb_1_ 
                    |where tb_1_.STORE_ID in (?, ?) 
                    |group by tb_1_.STORE_ID""".trimMargin()
            )
            rows(
                """[
                    |--->{
                    |--->--->"id":1,
                    |--->--->"name":"O'REILLY",
                    |--->--->"version":0,
                    |--->--->"avgPrice":58.500000000000,
                    |--->--->"website":null
                    |--->},{
                    |--->--->"id":2,
                    |--->--->"name":"MANNING",
                    |--->--->"version":0,
                    |--->--->"avgPrice":80.333333333333,
                    |--->--->"website":null
                    |--->}
                    |]""".trimMargin()
            )
        }
    }

    @Test
    fun testFetchKotlinFormula() {
        executeAndExpect(
            sqlClient.createQuery(Author::class) {
                where(table.firstName eq "Alex")
                select(
                    table.fetchBy {
                        fullName()
                    }
                )
            }
        ) {
            statement(0).sql(
                """select tb_1_.ID, tb_1_.FIRST_NAME, tb_1_.LAST_NAME 
                    |from AUTHOR tb_1_ 
                    |where tb_1_.FIRST_NAME = ?""".trimMargin()
            )
            rows(
                """[
                    |--->{
                    |--->--->"id":2,
                    |--->--->"fullName":"Alex Banks"
                    |--->}
                    |]""".trimMargin()
            )
        }
    }

    @Test
    fun testFetchSqlFormula() {
        executeAndExpect(
            sqlClient.createQuery(Author::class) {
                where(table.firstName eq "Alex")
                select(
                    table.fetchBy {
                        fullName2()
                    }
                )
            }
        ) {
            statement(0).sql(
                """select tb_1_.ID, concat(tb_1_.FIRST_NAME, ' ', tb_1_.LAST_NAME) 
                    |from AUTHOR tb_1_ 
                    |where tb_1_.FIRST_NAME = ?""".trimMargin()
            )
            rows("[{\"id\":2,\"fullName2\":\"Alex Banks\"}]")
        }
    }

    @Test
    fun testSqlFormulaWithBlank() {
        executeAndExpect(
            sqlClient.createQuery(Author::class) {
                where(table.firstName eq "Alex")
                select(
                    table.fetchBy {
                        fullNameLength()
                    }
                )
            }
        ) {
            statement(0).sql(
                """select tb_1_.ID, length(tb_1_.FIRST_NAME) + length(tb_1_.LAST_NAME) 
                    |from AUTHOR tb_1_ 
                    |where tb_1_.FIRST_NAME = ?""".trimMargin()
            )
            rows("[{\"id\":2,\"fullNameLength\":9}]")
        }
    }

    @Test
    fun testFetchIdView() {
        executeAndExpect(
            sqlClient
                .createQuery(Book::class) {
                    where(table.id eq 12L)
                    select(
                        table.fetchBy {
                            allScalarFields()
                            storeId()
                            authorIds()
                        }
                    )
                }
        ) {
            sql(
                """select tb_1_.ID, tb_1_.NAME, tb_1_.EDITION, tb_1_.PRICE, tb_1_.STORE_ID 
                    |from BOOK tb_1_ 
                    |where tb_1_.ID = ?""".trimMargin()
            )
            statement(1).sql(
                """select tb_1_.AUTHOR_ID 
                    |from BOOK_AUTHOR_MAPPING tb_1_ 
                    |where tb_1_.BOOK_ID = ?""".trimMargin()
            )
            rows(
                """[
                    |--->{
                    |--->--->"id":12,
                    |--->--->"name":"GraphQL in Action",
                    |--->--->"edition":3,
                    |--->--->"price":80.00,
                    |--->--->"storeId":2,
                    |--->--->"authorIds":[5]
                    |--->}
                    |]""".trimMargin()
            )
        }
    }

    @Test
    fun testByTransientAssociation() {
        executeAndExpect(
            sqlClient.createQuery(BookStore::class) {
                select(table.fetchBy {
                    allScalarFields()
                    newestBooks {
                        allScalarFields()
                    }
                })
            }
        ) {
            statement(0).sql(
                """select tb_1_.ID, tb_1_.NAME, tb_1_.VERSION, tb_1_.WEBSITE 
                    |from BOOK_STORE tb_1_""".trimMargin()
            )
            statement(1).sql(
                """select tb_1_.ID, tb_2_.ID 
                    |from BOOK_STORE tb_1_ 
                    |inner join BOOK tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
                    |where (tb_2_.NAME, tb_2_.EDITION) in (
                    |--->select tb_3_.NAME, max(tb_3_.EDITION) 
                    |--->from BOOK tb_3_ 
                    |--->where tb_3_.STORE_ID in (?, ?) 
                    |--->group by tb_3_.NAME
                    |)""".trimMargin()
            )
            statement(2).sql(
                """select tb_1_.ID, tb_1_.NAME, tb_1_.EDITION, tb_1_.PRICE 
                    |from BOOK tb_1_ 
                    |where tb_1_.ID in (?, ?, ?, ?)""".trimMargin()
            )
            rows(
                """[
                    |--->{
                    |--->--->"id":1,"name":"O'REILLY",
                    |--->--->"version":0,
                    |--->--->"website":null,
                    |--->--->"newestBooks":[
                    |--->--->--->{
                    |--->--->--->--->"id":3,
                    |--->--->--->--->"name":"Learning GraphQL",
                    |--->--->--->--->"edition":3,
                    |--->--->--->--->"price":51.00
                    |--->--->--->},{
                    |--->--->--->--->"id":6,
                    |--->--->--->--->"name":"Effective TypeScript",
                    |--->--->--->--->"edition":3,
                    |--->--->--->--->"price":88.00
                    |--->--->--->},{
                    |--->--->--->--->"id":9,
                    |--->--->--->--->"name":"Programming TypeScript",
                    |--->--->--->--->"edition":3,
                    |--->--->--->--->"price":48.00
                    |--->--->--->}
                    |--->--->]
                    |--->},{
                    |--->--->"id":2,
                    |--->--->"name":"MANNING",
                    |--->--->"version":0,
                    |--->--->"website":null,
                    |--->--->"newestBooks":[
                    |--->--->--->{
                    |--->--->--->--->"id":12,
                    |--->--->--->--->"name":"GraphQL in Action",
                    |--->--->--->--->"edition":3,
                    |--->--->--->--->"price":80.00
                    |--->--->--->}
                    |--->--->]
                    |--->}
                    |]""".trimMargin()
            )
        }
    }

    @Test
    fun testIssue714() {
        executeAndExpect(
            sqlClient.createQuery(BookStore::class) {
                select(table.fetchBy {
                    allScalarFields()
                    newestBookIds()
                })
            }
        ) {
            sql(
                """select tb_1_.ID, tb_1_.NAME, tb_1_.VERSION, tb_1_.WEBSITE 
                    |from BOOK_STORE tb_1_""".trimMargin()
            )
            statement(1).sql(
                """select tb_1_.ID, tb_2_.ID 
                    |from BOOK_STORE tb_1_ 
                    |inner join BOOK tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
                    |where (tb_2_.NAME, tb_2_.EDITION) in (
                    |--->select tb_3_.NAME, max(tb_3_.EDITION) 
                    |--->from BOOK tb_3_ 
                    |--->where tb_3_.STORE_ID in (?, ?) 
                    |--->group by tb_3_.NAME
                    |)""".trimMargin()
            )
            rows(
                """[{
                    |--->"id":1,
                    |--->"name":"O'REILLY",
                    |--->"version":0,
                    |--->"website":null,
                    |--->"newestBookIds":[3,6,9]
                    |},{
                    |--->"id":2,
                    |--->"name":"MANNING",
                    |--->"version":0,
                    |--->"website":null,
                    |--->"newestBookIds":[12]
                    |}]""".trimMargin()
            )
        }
    }

    @Test
    fun testFlatIdForIssue671() {
        executeAndExpect(
            sqlClient.createQuery(Dependency::class) {
                select(table.fetch(DependencyView::class))
            }
        ) {
            sql(
                """select tb_1_.GROUP_ID, tb_1_.ARTIFACT_ID, tb_1_.VERSION, tb_1_.SCOPE 
                    |from DEPENDENCY tb_1_""".trimMargin()
            )
            rows(
                """[{
                    |"version":"0.8.177",
                    |"scope":"COMPILE",
                    |"groupId":"org.babyfish.jimmer",
                    |"artifactId":"jimmer-sql-kotlin"}
                    |]""".trimMargin()
            )
        }
    }
}
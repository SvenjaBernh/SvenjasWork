package week09.dbms.indexing

import org.scalatest.funsuite.AnyFunSuite

import week09.dbms.misc.{DBType, IndexType, Variant}
import week09.dbms.store.{Schema, Table, TableRecord}

class UnbalancedTreeIndexSuite extends AnyFunSuite {

  private val attributes = Seq("salary", "bonus", "department")
  private val types = Seq(DBType.Double, DBType.Int, DBType.String)
  private val records = Seq(
    TableRecord(Seq(
      "salary" -> Variant(3600.0),
      "bonus" -> Variant(300),
      "department" -> Variant("software")
    )),
    TableRecord(Seq(
      "salary" -> Variant(4200.0),
      "bonus" -> Variant(500),
      "department" -> Variant("marketing")
    )),
    TableRecord(Seq(
      "salary" -> Variant(3870.0),
      "bonus" -> Variant(300),
      "department" -> Variant("software")
    )),
    TableRecord(Seq(
      "salary" -> Variant(3000.0),
      "bonus" -> Variant(900),
      "department" -> Variant("management")
    )),
    TableRecord(Seq(
      "salary" -> Variant(3600.0),
      "bonus" -> Variant(0),
      "department" -> Variant("distribution")
    ))
  )

  private val schema = new Schema(attributes.zip(types))

  test(
    "the index on the empty table should only yield empty sequences for each key"
  ) {
    val emptyTable = Table(schema)
    emptyTable.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    assert(emptyTable.filterByIndex(Seq("salary"), "bonus", Variant(300)).isEmpty)
  }

  test("clearing the empty index should leave it empty") {
    val emptyTable = Table(schema)
    emptyTable.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    emptyTable.clear()
    assert(emptyTable.filterByIndex(Seq("salary"), "bonus", Variant(300)).isEmpty)
  }

  test(
    "the index on the table with records should point to the correct records when reading it"
  ) {
    val table = Table(schema, records)
    table.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    val query = table.filterByIndex(Seq("salary"), "bonus", Variant(300))
    assert(query.size == 2)
    val expected = table.filterByScan(Seq("salary"), "bonus", Variant(300))
    assert(query.toSet == expected.toSet)
  }

  test(
    "the index should yield an empty sequence if you filter a non-empty table for a value bigger than all contained"
  ) {
    val table = Table(schema, records)
    table.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    val query = table.filterByIndex(Seq("salary"), "bonus", Variant(7000))
    assert(query.isEmpty)
  }

  test(
    "the index should yield an empty sequence if you filter a non-empty table for a value smaller than all contained"
  ) {
    val table = Table(schema, records)
    table.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    val query = table.filterByIndex(Seq("salary"), "bonus", Variant(-400))
    assert(query.isEmpty)
  }

  test(
    "the index should work correctly when the indexing column contains strings"
  ) {
    val table = Table(schema, records)
    table.createIndex("department", IndexType.UnbalancedTreeIndex)
    val query =
      table.filterByIndex(Seq("salary"), "department", Variant("management"))
    val expected =
      table.filterByScan(Seq("salary"), "department", Variant("management"))
    assert(query == expected)
  }

  test(
    "the index should find records inserted after the creation of the index"
  ) {
    val table = Table(schema, records)
    table.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    val record = TableRecord(Seq(
      "salary" -> Variant(2600.0),
      "bonus" -> Variant(700),
      "department" -> Variant("marketing")
    ))
    table.addOne(record)
    val query = table.filterByIndex(Seq("salary"), "bonus", Variant(700))
    val expected = table.filterByScan(Seq("salary"), "bonus", Variant(700))
    assert(query == expected)
  }

  test("the index should clear itself when the table gets cleared") {
    val table = Table(schema, records)
    table.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    table.clear()
    val query = table.filterByIndex(Seq("salary"), "bonus", Variant(300))
    assert(query.isEmpty)
  }

  test("the index should find records inserted after it got cleared") {
    val table = Table(schema, records)
    table.createIndex("bonus", IndexType.UnbalancedTreeIndex)
    table.clear()
    val record = TableRecord(Seq(
      "salary" -> Variant(2600.0),
      "bonus" -> Variant(300),
      "department" -> Variant("marketing")
    ))
    table.addOne(record)
    val query = table.filterByIndex(Seq("salary"), "bonus", Variant(300))
    val expected = table.filterByScan(Seq("salary"), "bonus", Variant(300))
    assert(query.size == 1)
    assert(query == expected)
  }
}

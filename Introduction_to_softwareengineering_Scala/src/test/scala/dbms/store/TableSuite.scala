package week09.dbms.store

import org.scalatest.funsuite.AnyFunSuite
import week09.dbms.misc.DBType
import week09.dbms.misc.Variant
import week09.dbms.expressions.Variable
import week09.dbms.expressions.BinaryOperation
import week09.dbms.expressions.Constant
import week09.dbms.expressions.UnaryOperation

class TableSuite extends AnyFunSuite {

  private val schema = Schema(
    Seq(
      "name" -> DBType.String,
      "price" -> DBType.Double,
      "amount" -> DBType.Int,
      "discountPercentage" -> DBType.Double
    )
  )
  private val table = Table(
    schema,
    Seq(
      TableRecord(Seq("name" -> Variant("Apple"), "price" -> Variant(1.25), "amount" -> Variant(2500), "discountPercentage" -> Variant(0.01))),
      TableRecord(Seq("name" -> Variant("Orange"), "price" -> Variant(1.2), "amount" -> Variant(1200), "discountPercentage" -> Variant(0.0))),
      TableRecord(Seq("name" -> Variant("Mango"), "price" -> Variant(2.1), "amount" -> Variant(975), "discountPercentage" -> Variant(0.05))),
      TableRecord(Seq("name" -> Variant("Chili"), "price" -> Variant(1.9), "amount" -> Variant(3333), "discountPercentage" -> Variant(0.12))),
      TableRecord(Seq("name" -> Variant("Walnut"), "price" -> Variant(0.99), "amount" -> Variant(4566), "discountPercentage" -> Variant(0.07)))
    )
  )

  private val price = Variable("price")
  private val totalPriceNoDiscount = BinaryOperation("*", Variable("price"), Variable("amount"))
  private val totalPrice = BinaryOperation(
    "*",
    totalPriceNoDiscount,
    BinaryOperation(
      "+",
      Constant(1),
      UnaryOperation("-", Variable("discountPercentage"))
    )
  )
  private val invalid1 = BinaryOperation(
    "+",
    BinaryOperation(
      "*",
      totalPrice,
      Variable("name")
    ),
    totalPrice
  )
  private val invalid2 = UnaryOperation("abs", Variable("supplierID"))
  private val doubious = BinaryOperation(
    "*",
    BinaryOperation(
      "*",
      Variable("amount"),
      UnaryOperation("invert", Variable("discountPercentage"))
    ),
    Constant(100.0)
  )

  test("running the price query should result in the price of every item") {
    val result = table.evaluate(price)
    val expected = Seq(1.25, 1.2, 2.1, 1.9, 0.99)
    assert(result == expected)
  }

  test("running the query for the total price without the discount should result in the correct prices") {
    val result = table.evaluate(totalPriceNoDiscount)
    val expected = Seq(3125.0, 1440.0, 2047.5, 6332.7, 4520.34)
    assert(result == expected)
  }

  test("running the query for the total price with the discount should result in the correct prices") {
    val result = table.evaluate(totalPrice)
    val expected = Seq(3093.75, 1440.0, 1945.125, 5572.776, 4203.9162)
    assert(result == expected)
  }

  test("running the first invalid query should result in an error") {
    assertThrows[IllegalArgumentException] {
      table.evaluate(invalid1)
    }
  }

  test("running the second invalid query should result in an error") {
    assertThrows[IllegalArgumentException] {
      table.evaluate(invalid2)
    }
  }

  test("when the query throws an error for a row the value 0 should be the result for that row") {
    val result = table.evaluate(doubious)
    val expected = Seq(2.5E7, 0.0, 1950000.0, 2777500.0000000005, 6522857.142857143)
    assert(result == expected)
  }
}

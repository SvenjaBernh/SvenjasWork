package week09.dbms.expressions

import org.scalatest.funsuite.AnyFunSuite

class ExpressionSuite extends AnyFunSuite {

  private val sampleExpression = BinaryOperation(
    "+",
    UnaryOperation("invert", Variable("x")),
    BinaryOperation(
      "*",
      Constant(2),
      UnaryOperation("abs", Variable("y"))
    )
  )

  test("the addition of 2 constants should get simplified to a single constant of the sum") {
    val result = BinaryOperation("+", Constant(69), Constant(42)).simplified
    assert(result == Constant(111))
  }

  test("the multiplication of 2 constants should get simplified to a single constant of the product") {
    val result = BinaryOperation("*", Constant(42), Constant(2)).simplified
    assert(result == Constant(84))
  }

  test("the negation of a constant should get simplified to a constant of the negation") {
    val result = UnaryOperation("-", Constant(42)).simplified
    assert(result == Constant(-42))
  }

  test("the abs of a constant should get simplified to a constant of the abs") {
    val result = UnaryOperation("abs", Constant(-42)).simplified
    assert(result == Constant(42))
  }

  test("the invert of a constant should get simplified to a constant of the invert") {
    val result = UnaryOperation("invert", Constant(42)).simplified
    assert(result == Constant(1.0 / 42))
  }

  test("the invert of 0 should result in an error") {
    assertThrows[ArithmeticException] {
      UnaryOperation("invert", Constant(0)).simplified
    }
  }

  test("the addition of any term with the constant 0 should get simplified to the term") {
    val result = BinaryOperation("+", sampleExpression, Constant(0)).simplified
    assert(result == sampleExpression)
  }

  test("the multiplication of any term with the constant 1 should get simplified to the term") {
    val result = BinaryOperation("*", Constant(1), sampleExpression).simplified
    assert(result == sampleExpression)
  }

  test("the multiplication of any term with the constant 0 should get simplified to the constant 0") {
    assert(BinaryOperation("*", sampleExpression, Constant(0)).simplified == Constant(0))
    assert(BinaryOperation("*", Constant(0), sampleExpression).simplified == Constant(0))
  }

  test("the multiplication of any term with the constant -1 should get simplified to the unary negation") {
    val result = BinaryOperation("*", sampleExpression, Constant(-1)).simplified
    assert(result == UnaryOperation("-", sampleExpression))
  }

  test("double negation of any term should get simplified to the term") {
    val result = UnaryOperation("-", UnaryOperation("-", sampleExpression)).simplified
    assert(result == sampleExpression)
  }

  test("double abs of any term should get simplified to a single abs of the term") {
    val result = UnaryOperation("abs", UnaryOperation("abs", sampleExpression)).simplified
    assert(result == UnaryOperation("abs", sampleExpression))
  }

  test("double invert of any term should not get simplified away") {
    val expression = UnaryOperation("invert", UnaryOperation("invert", Variable("y")))
    assert(expression.simplified == expression)
  }
}

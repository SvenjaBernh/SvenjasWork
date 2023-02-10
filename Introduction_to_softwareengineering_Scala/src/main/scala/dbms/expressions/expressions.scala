package week09.dbms.expressions

import week09.dbms.expressions

sealed abstract class Expression {

  /** Returns a string representation of this [[Expression]] */
  override def toString: String = this match {
    case Variable(name)                   => name
    case Constant(value)                  => value.toString
    case UnaryOperation(op, ex)           => "(" + op + " " + ex.toString + ")"
    case UnaryOperation(op, ex)           => s"($op $ex)"
    case BinaryOperation(op, left, right) => s"($left $op $right)"
  }

  /** Evaluates this [[Expression]] with the specified variable bindings.
   *
   *  @param variables the variable bindings
   *  @return the final result
   */
  def evaluate(variables: Map[String, Double]): Double = this match {
    case Variable(name)                    => variables.getOrElse(name, throw new IllegalArgumentException(s"variable $name not found"))
    case Constant(value)                   => value
    case BinaryOperation("+", left, right) => left.evaluate(variables) + right.evaluate(variables)
    case BinaryOperation("*", left, right) => left.evaluate(variables) * right.evaluate(variables)
    case UnaryOperation("-", ex)           => - ex.evaluate(variables)
    case UnaryOperation("abs", ex)         => ex.evaluate(variables).abs
    case UnaryOperation("invert", ex)      =>
      val denominator = ex.evaluate(variables)
      if (denominator == 0) throw new ArithmeticException("the denominator evaluated to 0")
      1 / denominator
    case _ => throw new IllegalStateException("illegal operator")
  }

  /** Returns the names of all variables referenced by this expression. */
  def referencedVariables: Set[String] = this match {
    case Variable(name) => Set(name)
    case Constant(value) => Set()
    case UnaryOperation(op, ex) => ex.referencedVariables
    case BinaryOperation(op, left, right) => left.referencedVariables ++ right.referencedVariables
  }

  /** Transforms this [[Expression]] into an equivalent but potentially simpler expression.
   *
   *  @return the simplified expression
   */
  def simplified: Expression = this match {
    case BinaryOperation("+", Constant(0), term) => term
    case BinaryOperation("+", term, Constant(0)) => term
    case BinaryOperation("*", Constant(0), term) => Constant(0)
    case BinaryOperation("*", term, Constant(0)) => Constant(0)
    case BinaryOperation("*", Constant(1), term) => term
    case BinaryOperation("*", term, Constant(0)) => term
    case BinaryOperation("*", Constant(-1), term) => UnaryOperation("-", term)
    case BinaryOperation("*", term,Constant(-1)) => UnaryOperation("-", term)
    case UnaryOperation("-",UnaryOperation("-",term)) => term
    case UnaryOperation("abs", UnaryOperation("abs", term)) => UnaryOperation("abs", term)
    case BinaryOperation("+", Constant(value1), Constant(value2)) => Constant(value1 + value2)
    case BinaryOperation("*", Constant(value1), Constant(value2)) => Constant(value1 * value2)
    case UnaryOperation("-", Constant(value1)) => Constant(- value1)
    case UnaryOperation("abs", Constant(value1)) => Constant(value1 * -1)
    case UnaryOperation("invert", Constant(value1)) => if value1 == 0 then throw ArithmeticException("0 can't be inverted")
                                                       else Constant(1/value1)
    case e => e
  }

  def constantFolding: Expression = this match{

    case UnaryOperation("-", Constant(value1)) => Constant(-value1)


  }


  /** Recursively simplifies this [[Expression]]  */
  def fullySimplified: Expression = this match {
    case UnaryOperation(op, ex)           => UnaryOperation(op, ex.fullySimplified).simplified
    case BinaryOperation(op, left, right) => BinaryOperation(op, left.fullySimplified, right.fullySimplified).simplified
    case e => e
  }
}

/** Represents a variable, i.e. a value to be set later.
 *
 *  @param name the name of the variable
 */
case class Variable(name: String) extends Expression

/** Represents a constant, i.e. a fixed value.
 *
 *  @param value the value of the constant
 */
case class Constant(value: Double) extends Expression

/** Represents any operation that takes a single argument.
 *
 *  @param op a symbol representing the operator
 *  @param ex the argument
 */
case class UnaryOperation(op: String, ex: Expression) extends Expression

/** Represents any operation that takes two arguments.
 *
 *  @param op a symbol representing the operator
 *  @param left the left argument
 *  @param right the right argument
 */
case class BinaryOperation(op: String, left: Expression, right: Expression) extends Expression

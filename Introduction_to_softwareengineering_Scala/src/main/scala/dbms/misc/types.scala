package week09.dbms.misc

/** Represents the index/row-number of a record in a table. */
type RecordID = Int

/** Represents all available types of indexes. */
enum IndexType {
  case HashIndex, TreeIndex, UnbalancedTreeIndex
}

/** Represents all supported datatypes of the database */
enum DBType {
  case Int, Double, String
}

/** Represents all data type that can be used in the schema/table */
enum Variant(val dataType: DBType) extends Ordered[Variant], Serializable {
  /** Represents an Int type */
  case IntType(val i: Int) extends Variant(DBType.Int)

  /** Represents a Double type */
  case DoubleType(val d: Double) extends Variant(DBType.Double)

  /** Represents a String type */
  case StringType(d: String) extends Variant(DBType.String) 

  /** Compares this DBType with that DBType
   * 
   *  @param that the DBType to compare with
   *  @return 1 if this is larger than that, -1 if that is larger than this, and 0 otherwise 
   **/
  override def compare(that: Variant): Int = {
    (this, that) match {
      case (IntType(l), IntType(r)) => if (l > r) 1 else if (l < r) -1 else 0
      case (DoubleType(l), DoubleType(r)) => if (l > r) 1 else if (l < r) -1 else 0
      case (StringType(l), StringType(r)) => if (l > r) 1 else if (l < r) -1 else 0
      case _ => throw IllegalArgumentException("Comparison with incompatible type.")
    }
  }
}

object Variant {
  /** Returns a new IntType for the given integer */
  def apply(i: Int) = IntType(i)

  /** Returns a new DoubleType for the given double */
  def apply(d: Double) = DoubleType(d)

  /** Returns a new String Type for the given String */
  def apply(s: String) = StringType(s)
}

/** Represents a point in time */
type Timestamp = java.time.LocalDateTime
package week09.dbms.store

import week09.dbms.misc.Variant

/** Represents an individual immutable record
 * 
 * @param elems a sequence of (attribute, value) tuples
 */ 
class TableRecord(elems: Seq[(String, Variant)]) extends Serializable {
  if (elems.isEmpty)
    throw IllegalArgumentException("Cannot create a record without attributes")

  val schema = Schema(elems.map((attribute: String, value: Variant) => attribute -> value.dataType))

  /** Maps each attribute of the record to its value. */
  protected val attributes = elems.toMap

  /** Returns the number of attributes of this record. */
  val numAttributes = elems.size

  /** Returns true iff the record contains the given attribute. */
  def hasAttribute(attribute: String): Boolean = attributes.contains(attribute)

  /** Returns the value of a specific attribute.
   *
   *  @param attribute the name of the attribute to get the value for
   *  @return the value which corresponds to this attribute.
   *  @throws IllegalArgumentException if the passed attribute is unknown.
   */
  def getValue(attribute: String): Variant = {
    attributes.getOrElse(attribute, throw IllegalArgumentException("Passed attribute name is unknown."))
  }

  /** Returns the value of a specific attribute.
   *
   *  @param attribute the name of the attribute to get the value for
   *  @return the value which corresponds to this attribute.
   *  @throws IllegalArgumentException if the passed attribute is unknown.
   */
  def apply(attribute: String): Variant = getValue(attribute)

  /** Returns the textual representation of the record. */
  override def toString: String = attributes.mkString("(", ", ", ")")

  /** Returns whether an object equals this record. */
  override def equals(that: Any): Boolean = {
    that match {
      case record: TableRecord => record.attributes == this.attributes
      case _ => false
    }
  }

  override def hashCode: Int = attributes.hashCode
}
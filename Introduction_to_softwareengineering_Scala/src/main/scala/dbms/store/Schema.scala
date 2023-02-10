package week09.dbms.store

import week09.dbms.misc.DBType

/** Represents an immutable schema 
 * 
 * @param elems a sequence of (attribute name, attribute type) pairs
 */
class Schema(elems: Seq[(String, DBType)]) extends Serializable {
  if (elems.isEmpty) 
    throw IllegalArgumentException("Cannot create a schema without attributes.")

  if (elems.size != elems.distinctBy((attribute: String, dataType: DBType) => attribute).size)
    throw IllegalArgumentException("Passed attributes are not unique.")

  /** Maps each attribute to its data type*/
  protected val dataTypes = elems.toMap

  /** All attribute names of this schema */
  val attributes: Seq[String] = dataTypes.keys.toSeq

  /** The number of attributes of the schema */
  val numAttributes = attributes.size

  /** Returns whether an attribute with the specified attributeName is part of the schema */
  def contains(attribute: String): Boolean = attributes.contains(attribute)

  /** Returns the DBType associated with the passed attribute */
  def getDataType(attribute: String): DBType = dataTypes
    .getOrElse(attribute, throw IllegalArgumentException("The passed attribute is not part of the schema")) 

  /** Returns a new schema containing only the passed subsetOfAttributes */
  def getSubsetOfAttributes(subsetOfAttributes: Seq[String]): Schema = {
    if (subsetOfAttributes.exists((attribute: String) => !contains(attribute)))
      throw IllegalArgumentException("The provided subset of attribute is not a subset of the schema.")

    val filteredAttributes = dataTypes.filter((attribute: String, dataType: DBType) => subsetOfAttributes.contains(attribute))
    Schema(filteredAttributes.toSeq)
  }

  /** Returns whether that equals with this in terms of the maps
   *
   *  @param that the Schema to compare with
   *  @return true, iff this equals that 
   */ 
  override def equals(that: Any): Boolean = {
    that match {
      case thatSchema: Schema => this.dataTypes == thatSchema.dataTypes
      case _ => false
    }
  }

  /** Delegates to the hashCode of the attributes */ 
  override def hashCode: Int = dataTypes.hashCode
}
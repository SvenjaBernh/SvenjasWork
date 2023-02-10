package week09.dbms.indexing

import week09.dbms.misc.{Variant, RecordID, IndexType}
import week09.dbms.store.Table

import collection.mutable.HashMap

/** Represents an index, that is internally materialized as hash map
 * 
 * @param table the table on which the index is built
 * @param attribute the name of the attribute to build the index on
 */ 
class HashIndex(table: Table, attribute: String) extends MapBasedIndex(table, attribute) {
  /** The internal datastructure (a HashMap) used to represent our index data. */
  protected val index: HashMap[Variant, Seq[RecordID]] = getIndexMapping.to(HashMap)

  /** Returns the type of this index */
  override def indexType: IndexType = IndexType.HashIndex
}
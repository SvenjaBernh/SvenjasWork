package week09.dbms.indexing

import week09.dbms.misc.{Variant, RecordID, IndexType}
import week09.dbms.store.Table

import collection.mutable.TreeMap

/** Represents an index, that is internally materialized as a search tree
 * 
 * @param table the table on which the index is built
 * @param attribute the name of the attribute to build the index on
 */ 
class TreeIndex(table: Table, attribute: String) extends MapBasedIndex(table, attribute), IsRangeIndex {
  /** The internal datastructure (a TreeMap) used to represent our index data. */
  val index: TreeMap[Variant, Seq[RecordID]] = getIndexMapping.to(TreeMap)

  /** Returns all recordIDs that fall within the given key range
   * 
   * @param inclusiveLowerKey the inclusive lower key boundary
   * @param exclusiveUpperKey the exclusive upper key boundary
   * @return a sequence of all recordIDs associated with the given key (can be empty if key is not indexed)
   */ 
  override def getRange(inclusiveLowerKey: Variant, exclusiveUpperKey: Variant): Seq[RecordID] = {
    index.range(inclusiveLowerKey, exclusiveUpperKey)
      .flatMap((key, recordIDs) => recordIDs)
      .toSeq
  }

  /** Returns the type of this index */
  override def indexType: IndexType = IndexType.TreeIndex
}
package week09.dbms.store

/** Represents an iterator over a table
 * 
 *  @param table the table to iterator over
 */
class TableIterator(table: Table) extends Iterator[TableRecord] {
  private var currentRecordID = 0
  private val tableSize = table.numRecords // appends to the table after iterator construction should not be visible
  
  override def hasNext: Boolean = currentRecordID < tableSize
  
  override def next(): TableRecord = {
    val record = table.getRecord(currentRecordID)
    currentRecordID += 1
    record
  }
}
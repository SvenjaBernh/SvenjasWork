package week09.dbms.store

import week09.dbms.misc.{DBType, IndexType, RecordID, Variant}
import week09.dbms.indexing.{HashIndex, IsIndex, IsRangeIndex, TreeIndex, UnbalancedTreeIndex}
import week09.dbms.expressions.Expression

import collection.mutable.ArrayBuffer
import collection.immutable.{AbstractSeq, LinearSeq}
import collection.mutable

import java.io.*

/** Represents a table of the database
 * 
 * @constructor creates an empty table
 * @param schema the schema of the table
 */
class Table private (val schema: Schema) extends Iterable[TableRecord], mutable.Growable[TableRecord], Serializable {

  /** Alternative constructor to bulk-load the created table 
   *  
   *  @param schema the schema of the table
   *  @param initialRecords a sequence of records to append to the table
   */
  private def this(schema: Schema, initialRecords: Seq[TableRecord]) = {
    this(schema)
    initialRecords.foreach((r: TableRecord) => appendRecord(r))
  }

  /** Holds all records of the table in an ordered fashion. */
  private val records: ArrayBuffer[TableRecord] = ArrayBuffer()

  /** Returns the number of records that are currently present in the table. */
  def numRecords: Int = records.size 

  /** Maps an attribute name to an index */
  private val indexes: collection.mutable.Map[String, IsIndex] = collection.mutable.Map()

  /** Returns an iterator over all records of the table (from Iterable) */
  override def iterator: Iterator[TableRecord] = TableIterator(this) // we could also delegate to records.iterator here

  override def addOne(r: TableRecord): this.type = {
    appendRecord(r)
    this
  }

  override def clear(): Unit = {
    records.clear()
    indexes.foreach((attr, index) => index.clear())
  }

  override def knownSize: Int = numRecords

  /** Appends a given record to the table.
   *
   * @param record the Record to append
   * @return the RecordID of the record in the table.
   */
  def appendRecord(record: TableRecord): RecordID = {
    if (this.schema != record.schema)
      throw IllegalArgumentException("The passed record has a different schema than the table.")

    records.append(record)
    val recordID = numRecords - 1

    // update all existing indexes to reflect the newly inserted record
    indexes.foreach((attribute: String, index: IsIndex) => index.add(record.getValue(attribute), recordID))

    recordID
  }

  /** Creates a record from the given (attribute, value) tuples and appends it to the table.
   *
   * @param elems a sequence of (attribute, value) tuples
   * @return the RecordID of the record in the table.
   */
  def appendRecord(elems: Seq[(String, Variant)]): RecordID = {
    appendRecord(TableRecord(elems))
  }

  /** Returns a record for a given recordID. */
  def getRecord(recordID: RecordID): TableRecord = {
    val recordIDs: Range = records.indices
    val validRecordID: Boolean = recordIDs.contains(recordID)
    if !(validRecordID) then
      throw IllegalArgumentException("The passed recordID is out of bounds.")

    records(recordID)
  }

  /** Creates an index of a specified type on a specified attribute
   *
   * @param attribute the attribute name of the attribute on which the index should be created
   * @param indexType the type of index to create
   * @return true if a new index was actually created. Returns false, if an index already exists on the attribute.
   */
  def createIndex(attribute: String, indexType: IndexType): Unit = {
    if (!schema.contains(attribute))
      throw IllegalArgumentException("This attribute does not exist in this table.")
    if (indexes.contains(attribute))
      throw IllegalArgumentException("An index already exists for the specified attribute.")

    val newIndex: IsIndex = indexType match {
      case IndexType.HashIndex => HashIndex(this, attribute)
      case IndexType.TreeIndex => TreeIndex(this, attribute)
      case IndexType.UnbalancedTreeIndex => ???
    }

    indexes.update(attribute, newIndex)
  }

  /** Returns some type of index on a given attribute, or none, if no index exists on the attribute. */
  def getIndexTypeOnAttribute(attribute:String): Option[IndexType] = {
    indexes.get(attribute) match {
      case Some(index: IsIndex) => Some(index.indexType)
      case None => None
    }
  }

  /** Filters the table using a scan with respect to a key.
   *
   *  @param outputAttributes all attributes to output
   *  @param selectionAttribute the attribute to filter on
   *  @param key the key to match on selectionAttribute, such that the record becomes part of the result
   *  @return the result table
   */
  def filterByScan(outputAttributes: Seq[String], selectionAttribute: String, key: Variant): Table = {
    if (key.dataType != schema.getDataType(selectionAttribute))
      throw IllegalArgumentException("incompatible type passed to filter")
    val outputSchema = schema.getSubsetOfAttributes(outputAttributes)
    val result =
      filter(record => record.getValue(selectionAttribute) == key)
      .map(record => outputAttributes.map(attr => attr -> record.getValue(attr)))
      .map(values => TableRecord(values))
      .toSeq
    Table(outputSchema, result)
  }

  /** Filters the table using a scan with respect to a key range.
   *
   *  @param outputAttributes all attributes to output
   *  @param selectionAttribute the attribute to filter on
   *  @param inclusiveLowerKey the inclusive lower key boundary on the selectionAttribute
   *  @param exclusiveUpperKey the exclusive upper key boundary on the selectionAttribute
   *  @return the result table
   */
  def filterRangeByScan(outputAttributes: Seq[String], selectionAttribute: String, inclusiveLowerKey: Variant, exclusiveUpperKey: Variant): Table = {
    if (inclusiveLowerKey.dataType != schema.getDataType(selectionAttribute))
      throw IllegalArgumentException("incompatible type passed to filter")
    if (exclusiveUpperKey.dataType != schema.getDataType(selectionAttribute))
      throw IllegalArgumentException("incompatible type passed to filter")
    val outputSchema = schema.getSubsetOfAttributes(outputAttributes)
    val result =
      filter(record => record.getValue(selectionAttribute) >= inclusiveLowerKey)
      .filter(record => record.getValue(selectionAttribute) < exclusiveUpperKey)
      .map(record => outputAttributes.map(attr => attr -> record.getValue(attr)))
      .map(values => TableRecord(values))
      .toSeq
    Table(outputSchema, result)
  }

  /** Filters the table using the index with respect to a key.
   *
   *  @param outputAttributes all attributes to output
   *  @param selectionAttribute the attribute to filter on
   *  @param key the key to match on selectionAttribute, such that the record becomes part of the result
   *  @return the result table
   */
  def filterByIndex(outputAttributes: Seq[String], selectionAttribute: String, key: Variant): Table = {
    // create a schema that contains only the output attributes
    val outputSchema = schema.getSubsetOfAttributes(outputAttributes)
    indexes.get(selectionAttribute) match {
      case Some(index: IsIndex) => {
        // retrieve all records that qualify with respect to the key
        val qualifyingRecords = index
          .get(key)
          .map(recordID => this.getRecord(recordID))
        // for each qualifying record, produce a corresponding output record with the output schema
        val outputRecords = qualifyingRecords
          .map(record => outputAttributes.map(attr => attr -> record.getValue(attr)))
          .map(values => TableRecord(values))
        // bulk-load a new table (which has the output schema) with the output records
        Table(outputSchema, outputRecords)
      }
      case None => throw IllegalArgumentException("There is no index available for this attribute!")
    }
  }

  /** Filters the table using the index with respect to a key range.
   *
   *  @param outputAttributes all attributes to output
   *  @param selectionAttribute the attribute to filter on
   *  @param inclusiveLowerKey the inclusive lower key boundary on the selectionAttribute
   *  @param exclusiveUpperKey the exclusive upper key boundary on the selectionAttribute
   *  @return the result table
   */
  def filterRangeByIndex(outputAttributes: Seq[String], selectionAttribute: String, inclusiveLowerKey: Variant, exclusiveUpperKey: Variant): Table = {
    // create a schema that contains only the output attributes
    val outputSchema = schema.getSubsetOfAttributes(outputAttributes)
    indexes.get(selectionAttribute) match {
      case Some(index: IsRangeIndex) => {
        // we have a IsRangeIndex available, so we can use it to retrieve the qualifying records
        val qualifyingRecords = index
          .getRange(inclusiveLowerKey, exclusiveUpperKey)
          .map(recordID => getRecord(recordID))
        // for each qualifying record, produce a corresponding output record with the output schema
        val outputRecords = qualifyingRecords
          .map(record => outputAttributes.map(attr => attr -> record.getValue(attr)))
          .map(values => TableRecord(values))
        // bulk-load a new table (which has the output schema) with the output records
        Table(outputSchema, outputRecords)
      }
      case Some(_) => throw IllegalArgumentException("There is no index supporting range queries available for this attribute!")
      case None => throw IllegalArgumentException("There is no index available for this attribute!")
    }
  }

  /** Returns a textual representation of all records currently stored in the table. */
  override def toString: String = records.mkString("Table:\n", "\n", "")

  /** Returns whether that equals with this in terms of the stored records
   *
   *  @param that the object to compare with
   *  @return true if this equals that, false otherwise
   */
  override def equals(that: Any): Boolean = {
    that match {
      case table: Table => records == table.records
      case _ => false
    }
  }

  override def hashCode: Int = records.hashCode()

  /** Serializes this table to a binary file on the disk. 
   * 
   *  @param path The path at which the file should be created.  
   */ 
  def storeTableToDisk(path: String): Unit = {
    val fileOutputStream: FileOutputStream = FileOutputStream(path)
    val objectOutputStream: ObjectOutputStream = ObjectOutputStream(fileOutputStream)
    try {
      objectOutputStream.writeObject(this)
      objectOutputStream.flush()
    } finally {
      print("Closing streams... ")
      // close all resources associated with these streams
      objectOutputStream.close()
      fileOutputStream.close()
      println("Done!")
    }
  }

  /** Write a human-readable file of this table to disk.
   * 
   *  @param path The path at which the file should be created.  
   */ 
  def writeToTextFile(path: String): Unit = {
    val printWriter: PrintWriter = PrintWriter(path)
    try {
      printWriter.write(this.toString)
    } finally {
      printWriter.close()
    }
  }

  /** Returns a new table column containing the result of evaluating the specified computation for each row of this table.
   *
   *  @param computation the computation to run for each row
   *  @throws IllegalArgumentException if the computation references an unknown column
   *  @throws IllegalArgumentException if the computation references a non-numeric column
   */
  def evaluate(computation: Expression): Seq[Double] = {
    ???
    /*var result: Seq[Double] = Seq()
    val variables: Seq[String] = computation.referencedVariables.toSeq
    val bindings: Map[String, Double] = records.flatMap(record => variables.map( variable => variable -> record.getValue(variable).toDouble)).toMap
    if !variables.forall(variable => schema.contains(variable)) then throw IllegalArgumentException("computation references an unknown column")
    if !variables.forall(variable => schema.getDataType(variable) == DBType.Int || schema.getDataType(variable) == DBType.Double)
      then throw IllegalArgumentException("computation references a non-numeric column")
    // NoSuchElementException wird nicht gefunden, daher Illegal argument
    try {
      this.records.foreach(record =>
        result.appended(computation.evaluate(bindings)))
    } catch
        case x: ArithmeticException => result.appended(0)
    result
    */
  }
}

/** Companion object of the class table */
object Table {

  /** Creates an empty table object from a given schema
   * 
   *  @param schema the schema of the table to create
   *  @return the newly created table
   */ 
  def apply(schema: Schema) = new Table(schema)

  /** Creates an table object from a given schema containing the initialRecords
   * 
   *  @param schema the schema of the table to create
   *  @param initialRecords the records to fill the new table with
   *  @return the newly created table
   */ 
  def apply(schema: Schema, initialRecords: Seq[TableRecord]) = new Table(schema, initialRecords)

  /** Returns a table object from a file on disk 
   * 
   *  @param path the path to the file representing the table to load 
   */
  def apply(path: String): Table = {
    val fileInputStream = FileInputStream(path)
    val objectInputStream = ObjectInputStream(fileInputStream)
    try {
      val readObject: Object = objectInputStream.readObject // The type "Object" is the Java equivalent to "Any"
      readObject.asInstanceOf[Table] // We cast "Object" to "Table"
    } finally {
      print("Closing streams... ")
      // close all resources associated with these streams
      objectInputStream.close()
      fileInputStream.close()
      println("Done!")
    }
  }
}
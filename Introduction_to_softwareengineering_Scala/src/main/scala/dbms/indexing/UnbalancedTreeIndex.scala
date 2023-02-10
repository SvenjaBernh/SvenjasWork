package week09.dbms.indexing

import week09.dbms.datastructures.UnbalancedSearchTree
import week09.dbms.misc.{DBType, IndexType, RecordID, Variant}
import week09.dbms.store.Table

class UnbalancedTreeIndex(table: Table, attribute: String)

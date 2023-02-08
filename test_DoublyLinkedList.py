
from DoublyLinkedList import DoublyLinkedList 
from DoublyLinkedList import Entry

#Test is_empty:
#leere List

def test_is_empty():
    '''test function is_empty on empty list'''
    liste = DoublyLinkedList()
    output = liste.is_empty()
    assert output == True

#nicht-leere List
def test_not_empty():
    '''test function is_empty on non empty list'''
    hund = DoublyLinkedList()
    hund.start_node = Entry("Waldi")
    output = hund.is_empty()
    assert output == False

#Test append
#append empty  list
def test_append():
    '''tests function append on empty list'''
    hunde = DoublyLinkedList()
    hunde.append("Anton")
    output = hunde.start_node.data 
    assert output == "Anton"

#append non empty list    
def test_append_more():
    '''test function append on non empty list'''
    hunde = DoublyLinkedList()
    hunde.start_node = Entry("Waldi")
    hunde.last_node = hunde.start_node
    hunde.append("Rudi")
    output = [hunde.start_node.data, hunde.start_node.right.data]
    assert output == ["Waldi","Rudi"]
    
#Test remove
#remove first element
def test_remove_first():
    '''test function remove on first element in list'''
    katzen = DoublyLinkedList()
    katzen.append("Jana")
    katzen.append("Liese")
    katzen.remove(0)
    output = katzen.start_node.data
    assert output == "Liese"
    
#remove last element
def test_remove_last():
    '''test function remove on last element in list'''
    katzen = DoublyLinkedList()
    katzen.append("Jana")
    katzen.append("Liese")
    katzen.remove(1)
    output = katzen.start_node.data
    assert output == "Jana"
    
#remove middle element
def test_remove_middle():
    '''tests function remove on middle element in list'''
    katzen = DoublyLinkedList()
    katzen.append("Jana")
    katzen.append("Liese")
    katzen.append("Mietze")
    katzen.remove(1)
    output = [katzen.start_node.data, katzen.start_node.right.data]
    assert output == ["Jana","Mietze"]
    
#test size
#empty list
def test_size_zero():
    '''test size on empty list'''
    horses = DoublyLinkedList()
    output = horses.size()
    assert output == 0
    
#one element
def test_size_one():
    '''test function size on list with one element'''
    horses = DoublyLinkedList()
    horses.append("Spirit")
    output = horses.size()
    assert output == 1

#more elements
def test_size_more():
    '''test function size on list with 3 elements'''
    horses = DoublyLinkedList()
    horses.append("Spirit")
    horses.append("Flicka")
    horses.append("Black Beauty")
    output = horses.size()
    assert output == 3

#test collect
def test_collect():
    '''test function collect on list with 3 elements'''
    horses = DoublyLinkedList()
    horses.append("Spirit")
    horses.append("Flicka")
    horses.append("Black Beauty")
    output = horses.collect()
    assert output == ["Spirit","Flicka","Black Beauty"]





class Entry():
    def __init__(self,data):
        #previous pointer
        self.left = None
        #data
        self.data = data
        #next pointer
        self.right = None
        

class DoublyLinkedList:

    def __init__(self):
        #first node
        self.start_node = None
        #last node
        self.last_node = None
        
    def is_empty(self):
        '''checks wether DoublyLinkedList is empty
        :return: boolean'''
        #empty if first node is empty
        if self.start_node == None :
            return True
        else: return False
        
    def append(self,data):
        '''adds data to the given DoublyLinkedList
        :input: any data'''
        #if first node is empty, data will be first node
        if self.start_node ==None :
            self.start_node = Entry(data)
            self.last_node = self.start_node
        #else data will be next node after current last node
        else:
            self.last_node.right = Entry(data)
            self.last_node.right.left = self.last_node
            self.last_node = self.last_node.right
            
    def remove(self,index):
        '''removes the data with the given index in given DoublyLInkedlist'''
        if index == 0:
            self.start_node = self.start_node.right
            self.start_node.left = None
        elif index == self.size()-1:
            self.last_node = self.last_node.left
            self.last_node.right = None
        else:
            start=self.start_node
            for i in range(index):
                start = start.right   
            start.left.right = start.right
            if start.right != None :
                start.right.left = start.left
            
    def size(self):
        '''returns the number of entries in the given doubly linked list
        :return: int'''
        counter = 0
        if self.is_empty() :
            return counter
        else:
            start = self.start_node
            while start != self.last_node.right:
                counter += 1
                start = start.right
            return counter
    
    def collect(self):
        '''returns the given doubly linked list
        :return: list'''
        liste = []
        start = self.start_node
        while start != None:
            liste.append(start.data)
            start = start.right
        return liste
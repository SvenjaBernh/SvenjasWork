class NumberField:
    '''Representation of a 2048 tile containing a number '''

    def __init__(self, number, x_coord, y_coord):
        '''initialises a new tile with the number number.'''
        self.number = number
        if number == 2: 
            self.color = 'lightgreen'
        if number == 4: 
            self.color = 'lightblue'
        if number == 8: 
            self.color = 'lightblue'
        if number == 16: 
            self.color = 'green'
        if number == 32: 
            self.color = 'green'
        if number == 64: 
            self.color = 'blue'
        if number == 128: 
            self.color = 'blue'
        if number == 256: 
            self.color = 'darkgreen'
        elif number == 512:
            self.color = 'darkgreen'
        elif number == 1024:
            self.color = 'darkblue'
        elif number == 2048:
            self.color = 'darkblue'
        else:
            raise Exception("Invalid tile number selected")
        self.coords = (x_coord, y_coord)

        self.translation = (0,0)

    def add(self, tile):
        '''creates a new tile if the two added tiles have the same number
        :return: NumberField'''
        return NumberField(self.number + tile.number, self.coords[0], self.coords[1])




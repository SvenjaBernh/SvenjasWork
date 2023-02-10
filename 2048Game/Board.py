#import NumberField
import random
import tkinter
from tkinter import Canvas, Label, Tk, StringVar, messagebox

class Board:
    '''Represents a 2048 board.
    The board consists of 4*4 tiles'''

    def __init__(self):
        self.tile_size = 100
        self.num_rows = 4
        self.num_cols = 4

        self.is_running = False

        self.score = 0

        self.tiles = [[0 for i in range(self.num_rows)] for j in range(self.num_cols)]
        self.freeTiles = 16

        #GUI
        self.root = Tk()
        self.root.title("2048")
        #status
        self.status_var = StringVar()
        self.status_var.set("Score: 0")
        self.status = Label(self.root, textvariable = self.status_var, font = ("Helvetica", 10, "bold"))
        self.status.pack()
        #canvas
        self.canvas = Canvas(
            self.root,
            width = self.num_cols * self.tile_size,
            height = self.num_rows * self.tile_size
        )

        self.canvas.pack()

    def drawCanvas(self):
        '''prints the board in its current status'''
        self.status_var.set("Score: %d" % self.score)
        self.status = Label(self.root, textvariable = self.status_var, font = ("Helvetica", 10, "bold"))
        for i in range(self.num_rows):
            for j in range(self.num_cols):    
                x1 = i*self.tile_size
                x2 = x1 + self.tile_size -5
                y1 = j * self.tile_size
                y2 = y1 + self.tile_size -5
                self.canvas.create_rectangle(x1, y1, x2, y2, outline = 'white', fill='lightblue')
                if self.tiles[i][j] != 0:
                    self.canvas.create_text((x1 +x2)/2, (y1 + y2)/2, font=('Arial', 30), fill='white', text="%d" % self.tiles[i][j], justify='center', )
        self.canvas.pack() 
        #react to keys
        self.root.bind("<Key>", self.handleEvents)

    def handleEvents(self, event):
        if event.keysym =="Left":
            self.moveTiles("Left")
        elif event.keysym =="Right":
            self.moveTiles("Right")
        elif event.keysym =="Up":
            self.moveTiles("Up")
        elif event.keysym =="Down":
            self.moveTiles("Down")

    def newPiece(self, x = None, y = None, number = 2):
        '''creates a new tile
        if no coordinates are given, a free random fiels gets chosen
        :return: number of the tile'''
        if number == None: number = 2
        if x == None or y == None:
            x = random.randrange(self.num_cols)
            y = random.randrange(self.num_rows)
            while self.tiles[x][y] != 0:
                x = random.randrange(self.num_cols)
                y = random.randrange(self.num_rows)
        self.tiles[x][y] = number
        return number

    def clearBoard(self):
        for i in range(self.num_cols):
            for j in range(self.num_rows):
                self.tiles[i][j] = 0
        
    def moveTiles(self, direction):
        '''moves all tiles in the direction given if possible'''
        match direction:
            case "Up":
                for i in range(self.num_cols):
                    for j in range(self.num_rows):
                        if self.tiles[i][j] != 0:
                            self.tryMove(i, j, "Up")
            case "Down":
                for i in range(self.num_cols):
                    for j in range(self.num_rows - 1, -1, -1):
                        if self.tiles[i][j] != 0:
                            self.tryMove(i, j, "Down")
            case "Right":
                for i in range(self.num_cols - 1, -1, -1):
                    for j in range(self.num_rows - 1, -1, -1):
                        if self.tiles[i][j] != 0:
                            self.tryMove(i, j, "Right")
            case "Left":
                for i in range(self.num_cols):
                    for j in range(self.num_rows):
                        if self.tiles[i][j] != 0:
                            self.tryMove(i, j, "Left")
            case _: raise Exception("Wrong direction passed")
        #is there still space?
        if self.freeTiles > 0: 
            self.newPiece()
            self.freeTiles -= 1
        self.drawCanvas()
        if self.gameOver(): self.endGame()

  
    def tryMove(self, x, y, direction):
        '''moves a single tile into the direction
        Tries to add tiles if there is a tile in the neighbour field'''
        stillMoving = True
        currentx = x
        currenty = y
        currentTile = self.tiles[x][y]
        while stillMoving == True:
            #check neighbour
            match direction:
                case "Up":
                    #is neighbour field in board?
                    if currenty != 0:
                        #is there a neighbour tile and is it the same number?
                        if self.tiles[currentx][currenty - 1] != 0 and self.tiles[currentx][currenty - 1] == currentTile:
                            self.tiles[currentx][currenty - 1] = self.newPiece(currentx, currenty - 1, currentTile + self.tiles[currentx][currenty - 1])
                            self.removeTile(currentx,currenty)
                            currenty = currenty - 1
                            currentTile = self.tiles[currentx][currenty]
                            self.score += currentTile
                            stillMoving = False
                        #is there a neighbour and is it a different number?
                        elif self.tiles[currentx][currenty - 1] != 0 and self.tiles[currentx][currenty - 1] != currentTile:
                            stillMoving = False
                        #no neighbour tile
                        elif self.tiles[currentx][currenty - 1] == 0:
                            self.tiles[currentx][currenty - 1] = self.newPiece(currentx, currenty - 1, currentTile)
                            self.removeTile(currentx,currenty)
                            currenty -= 1
                            currentTile = self.tiles[currentx][currenty]
                            self.score += currentTile   
                    #no neighbour field
                    else: stillMoving = False
                case "Down":
                    #is neighbour field in board?
                    if currenty != self.num_rows - 1:
                        #is there a neighbour tile and is it the same number?
                        if self.tiles[currentx][currenty + 1] != 0 and self.tiles[currentx][currenty + 1] == currentTile:
                            self.tiles[currentx][currenty + 1] = self.newPiece(currentx, currenty + 1, currentTile + self.tiles[currentx][currenty + 1])
                            self.removeTile(currentx,currenty)
                            currenty = currenty + 1
                            currentTile = self.tiles[currentx][currenty]
                            self.score += currentTile
                            stillMoving = False
                        #is there a neighbour and is it a different number?
                        elif self.tiles[currentx][currenty + 1] != 0 and self.tiles[currentx][currenty + 1] != currentTile:
                            stillMoving = False
                        #no neighbour tile
                        elif self.tiles[currentx][currenty + 1] == 0:
                            self.tiles[currentx ][currenty + 1] = self.newPiece(currentx, currenty + 1, currentTile)
                            self.removeTile(currentx,currenty)
                            currenty += 1
                            currentTile = self.tiles[currentx][currenty] 
                            self.score = self.score + currentTile  
                    #no neighbour field
                    else: stillMoving = False
                case "Right":
                    #is neighbour field in board?
                    if currentx != self.num_cols - 1:
                        #is there a neighbour tile and is it the same number?
                        if self.tiles[currentx + 1][currenty] != 0 and self.tiles[currentx + 1][currenty] == currentTile:
                            self.tiles[currentx + 1][currenty] = self.newPiece(currentx + 1, currenty, currentTile + self.tiles[currentx + 1][currenty])
                            self.removeTile(currentx,currenty)
                            currentx += 1
                            currentTile = self.tiles[currentx][currenty]
                            self.score += currentTile
                            stillMoving = False
                        #is there a neighbour and is it a different number?
                        elif self.tiles[currentx + 1][currenty] != 0 and self.tiles[currentx + 1][currenty] != currentTile:
                            stillMoving = False
                        #no neighbour tile
                        elif self.tiles[currentx + 1][currenty] == 0:
                            self.tiles[currentx + 1][currenty] = self.newPiece(currentx + 1, currenty, currentTile)
                            self.removeTile(currentx,currenty)
                            currentx += 1
                            currentTile = self.tiles[currentx][currenty] 
                            self.score += currentTile
                    #no neighbour field
                    else: stillMoving = False
                case "Left":
                    #is neighbour field in board?
                    if currentx != 0:
                        #is there a neighbour tile and is it the same number?
                        if self.tiles[currentx - 1][currenty] != 0 and self.tiles[currentx - 1][currenty] == currentTile:
                            self.tiles[currentx - 1][currenty] = self.newPiece(currentx - 1, currenty, currentTile + self.tiles[currentx - 1][currenty])
                            self.removeTile(currentx,currenty)
                            currentx -= 1
                            currentTile = self.tiles[currentx][currenty]
                            self.score += currentTile
                            stillMoving = False
                        #is there a neighbour and is it a different number?
                        elif self.tiles[currentx - 1][currenty] != 0 and self.tiles[currentx - 1][currenty] != currentTile:
                            stillMoving = False
                        #no neighbour tile
                        elif self.tiles[currentx - 1][currenty] == 0:
                            self.tiles[currentx - 1][currenty] = self.newPiece(currentx - 1, currenty, currentTile)
                            self.removeTile(currentx,currenty)
                            currentx -= 1
                            currentTile = self.tiles[currentx][currenty] 
                            self.score += currentTile  
                    #no neighbour field
                    else: stillMoving = False

    def removeTile(self, x, y):
        self.tiles[x][y] = 0
        self.freeTiles += 1

    def start(self):
        '''starts the game'''
        self.is_running = True
        self.score = 0
        self.clearBoard()
        self.newPiece()
        self.newPiece()
        self.freeTiles -= 2
        self.drawCanvas()

        self.root.mainloop()

    def gameOver(self):
        '''checks if the game is over/ if there are still moves possible
        :return: boolean'''
        for i in range(self.num_cols):
            for j in range(self.num_rows):
                if self.tiles[i][j] == 2048:
                    self.win()
        for i in range(self.num_cols):
            for j in range(self.num_rows):
                if self.tiles[i][j] == 0:
                    return False
        for i in range(self.num_cols):
            for j in range(self.num_rows):
                if i != 0:
                    if self.tiles[i][j] == self.tiles[i - 1][j]:
                        return False
                #down possible?
                if j != self.num_rows - 1:
                    if self.tiles[i][j] == self.tiles[i][j + 1]:
                        return False
        return True
                
    def endGame(self):
        messagebox.showinfo(
            "Game Over",
            "Punktzahl: %d." % self.score)
        self.canvas.delete(tkinter.ALL)
        self.start()

    def win(self):
        messagebox.showinfo(
            "You Won!",
            "Punktzahl: %d." % self.score)
        self.canvas.delete(tkinter.ALL)
        self.start()

            




package com.mod.util;

public class GuiHitbox {
    public int objectX;
    public int objectY;
    public int objectWidth;
    public int objectHeight;

    public GuiHitbox(int objectX, int objectY, int objectWidth, int objectHeight) {
        this.objectX = objectX;
        this.objectY = objectY;
        this.objectWidth = objectWidth;
        this.objectHeight = objectHeight;
    }

    public boolean isPointInsideHitbox(int mouseX, int mouseY)
    {
        return (mouseX >= this.objectX && mouseX < this.objectX + this.objectWidth &&
                mouseY >= this.objectY && mouseY < this.objectY + this.objectHeight);
    }

    //true if mouse is within x amount of pixels from the edge of the hitbox
    public boolean isPointInsideBorder(int mouseX, int mouseY, int borderThickness)
    {
        boolean isMouseWithinBorder = !(mouseX < this.objectX + borderThickness || mouseX > this.objectX + this.objectWidth - borderThickness ||
                mouseY < this.objectY + borderThickness || mouseY > this.objectY + this.objectHeight - borderThickness);

        return isPointInsideHitbox(mouseX, mouseY) && !isMouseWithinBorder;
    }

    public boolean collidesWith(GuiHitbox box)
    {
        boolean topLeft = box.isPointInsideHitbox(objectX, objectY);
        boolean topRight = box.isPointInsideHitbox(objectX + objectWidth, objectY);
        boolean bottomLeft = box.isPointInsideHitbox(objectX, objectY + objectHeight);
        boolean bottomRight = box.isPointInsideHitbox(objectX + objectWidth, objectY + objectHeight);
        return topLeft || topRight || bottomLeft || bottomRight;
    }

    public void moveToClosestLocationWithoutCollision(GuiHitbox box)
    {
        int[][] differences = new int[4][2];
        //calc the distance from each edge
        differences[0] = new int[]{-(objectX - (box.objectX + box.objectWidth)), 0};// left to right
        differences[1] = new int[]{-((objectX + objectWidth) - box.objectX), 0} ;// right to left
        differences[2] = new int[]{0, -(objectY - (box.objectY + box.objectHeight))};// top to bottom
        differences[3] = new int[]{0, -((objectY + objectHeight) - box.objectY)};// bottom to top

        //default is to snap to left side
        int[] lowest = differences[1];
        for (int[] direction : differences)
        {
            for (int difference : direction)
            {
                if (difference < lowest[0] || difference < lowest[1])
                {
                    lowest = direction;
                }
            }
        }

        objectX = lowest[0];
        objectY = lowest[1];
    }
}

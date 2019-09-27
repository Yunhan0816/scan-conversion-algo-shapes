//****************************************************************************
// SketchBase.  
//****************************************************************************

// Yunhan Huang
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SketchBase 
{
 public SketchBase()
 {
  // deliberately left blank
 }
   // draw a point
   public static void drawPoint(BufferedImage buff, Point2D p) {
     buff.setRGB(p.x, buff.getHeight() - p.y - 1, p.c.getBRGUint8());
   }
   
   
   public static void drawLine(BufferedImage buff, Point2D p1, Point2D p2) {
     drawPoint(buff, p1);
     drawPoint(buff, p2);

     int x = p1.x;
     int y = p1.y;
     int dx = Math.abs(p2.x - p1.x); // initialize dx and dy for p
     int dy = Math.abs(p2.y - p1.y); 
  
     // if point2 x or y is greater than point1 x or y, move positive 1, else move negative 1
     int deltaX = p2.x > p1.x ? 1 : -1;
     int deltaY = p2.y > p1.y ? 1 : -1; 
                                                                      
     int p; // initialize the decision parameter
     
     // compute the total color change
     float dr = (p2.c.r - p1.c.r);
     float dg = (p2.c.g - p1.c.g);
     float db = (p2.c.b - p1.c.b);
     
     // initialize the first color
     float r = p1.c.r, g = p1.c.g, b = p1.c.b;

     if (dx > dy) {
       // if slope is smaller than 1
       p = 2 * dy - dx;
       for (int i = 1; i < dx; i++) {
         r += dr / dx; 
         g += dg / dx;
         b += db / dx;
         x += deltaX;
         
         // two cases of decision parameter 
         if (p < 0) { // if p is negative, choose yk
           p += 2 * dy;
         } 
         else { // if p is positive, choose yk+1
           y += deltaY;
           p += 2 * dy - 2 * dx;
         }
         drawPoint(buff, new Point2D(x, y, new ColorType(r, g, b)));
       }
     } else {
       // if slope is greater than 1, swap the role of x and y
    	 p = 2 * dx - dy;  // initialize p
    	 for (int i = 1; i < dy; i++) { // increment 
    		 
	         r += dr / dy; // update r, g, b
	         g += dg / dy;
	         b += db / dy;
	         
	         y += deltaY; // update y
	         
	         if (p < 0) {
	        	 p += 2 * dx;
	         } 
	         else {
	        	 x += deltaX;
	        	 p += 2 * dx - 2 * dy;
	         }
         drawPoint(buff, new Point2D(x, y, new ColorType(r, g, b)));
       }
     }
   }

   
   // draw a triangle
   //drawing a general triangle is to decompose it into two triangles - 
   //a flat bottom triangle and a flat top triangle - and draw them both. 
   public static void drawTriangle
   (BufferedImage buff, Point2D p1, Point2D p2, Point2D p3, boolean smooth) {

	   if (p1.x==p2.x && p2.x==p3.x && p1.y==p2.y && p2.y==p3.y) {
		   drawPoint(buff,p1); //if three points overlap, draw p1
	   }
	    else if(p1.x==p2.x && p1.y==p2.y) {
	    	drawLine(buff,p1,p3); // if two points overlap, draw line between p1 to p3
	    }
	    else if(p2.x==p3.x && p2.y==p3.y) {
	    	drawLine(buff,p1,p3); // if two points overlap
	    }
	    else { // if no overlapping points
	    
	     // sort the vertices has to be performed at the first step
	     Point2D[] vs = sortvertices(p1, p2, p3);
	     Point2D v1 = vs[0];
	     Point2D v2 = vs[1];
	     Point2D v3 = vs[2];
	     
	     // if not smooth curve fill the triangle with the color of the first point
	     if (smooth != true) {
	       v1.c.r = p1.c.r;
	       v1.c.g = p1.c.g;
	       v1.c.b = p1.c.b;
	       
	       v2.c.r = p1.c.r;
	       v2.c.g = p1.c.g;
	       v2.c.b = p1.c.b;
	       
	       v3.c.r = p1.c.r;
	       v3.c.g = p1.c.g;
	       v3.c.b = p1.c.b;
	     }
	     
	     if (v1.y == v2.y) { // if the height of v1 and v2 are the same, fill triangle top 
	       fillTopTriangle(buff, v1, v2, v3);
	     }
	
	     else if (v2.y == v3.y) { // if the height of v2 and v3 are the same, fill triangle
	       fillBottomTriangle(buff, v1, v2, v3);
	     } 
	     
     else {
    	// split the triangle in a topflat and bottom-flat one 
       // we need to find a fourth vertex v4 
    //which is the intersection point of the boundary line and the long edge of the triangle
       float d = (float) (v2.y - v1.y) / (float) (v3.y - v1.y); 
     
       Point2D v4 = new Point2D((int) (v1.x + ((float) (v2.y - v1.y)/(float) (v3.y - v1.y)) 
           * (v3.x - v1.x)), v2.y,
    		   new ColorType(v3.c.r * (d) + (v1.c.r) 
        		   * (1 - d),v3.c.g * (d) + (v1.c.g) * (1 - d),v3.c.b 
        		   * (d) + (v1.c.b) * (1 - d)));

       // if not smooth, v4 is the same color with v1
       if (smooth!= true) {
           v4.c.r = p1.c.r;
           v4.c.g = p1.c.g;
           v4.c.b = p1.c.b;
       }
       
       fillBottomTriangle(buff, v1, v2, v4);
       fillTopTriangle(buff, v2, v4, v3);
       drawLine(buff, v2, v4); // draw the middle line
     }
    }
   }

   public static Point2D[] sortvertices(Point2D p1, Point2D p2, Point2D p3) {
     int[] ys = {p1.y, p2.y, p3.y}; // list of ys in each points 
     
     Point2D[] result = new Point2D[3];
     
     Arrays.sort(ys); // sort the ys 
     
     if (ys[0] == p1.y) {
    	 result[0] = p1;
    	 
    	 if (ys[1] == p2.y) {
    		 result[1] = p2;
    		 result[2] = p3;
    	 } 
    	 else {
    		 result[1] = p3;
    		 result[2] = p2;
    	 }
     } 
     else if (ys[0] == p3.y) {
    	 result[0] = p3;
    	 
    	 if (ys[1] == p1.y) {
    		 result[1] = p1;
    		 result[2] = p2;
    	 } 
    	 else {
    		 result[1] = p2;
    		 result[2] = p1;
    	 }
     }
     // when it equals to p2's height
     else if (ys[0] == p2.y) {
    	 result[0] = p2;
    	 
    	 if (ys[1] == p1.y) {
    		 result[1] = p1;
    		 result[2] = p3;
    	 } 
    	 else {
    		 result[1] = p3;
    		 result[2] = p1;
    	 }
     } 

     return result;
   }

   public static void fillBottomTriangle(BufferedImage buff, Point2D v1, Point2D v2, Point2D v3) {

     float slope1 = (float) (v2.x - v1.x) / (v2.y - v1.y);
     float slope2 = (float) (v3.x - v1.x) / (v3.y - v1.y);
     
     float curx1 = v1.x; // current x1 and x2 using v1 because fillbottom
     float curx2 = v1.x; 

     // compute the total color change
     float dr1 = (v2.c.r - v1.c.r), dg1 = (v2.c.g - v1.c.g), db1 = (v2.c.b - v1.c.b); //color change for v2 and v1
     float dr2 = (v3.c.r - v1.c.r), dg2 = (v3.c.g - v1.c.g), db2 = (v3.c.b - v1.c.b); // color change for v3 and v1
     
     // initialize the first color
     float r1 = v1.c.r, g1 = v1.c.g, b1 = v1.c.b;
     float r2 = v1.c.r, g2 = v1.c.g, b2 = v1.c.b;

     float dy = v2.y - v1.y; // compute difference of y
     
     for (int scanlineY = v1.y; scanlineY <= v2.y; scanlineY++) {
    	 	r1 += dr1 / dy; // update color 
	        g1 += dg1 / dy;
	        b1 += db1 / dy;
	        
	        r2 += dr2 / dy;
	        g2 += dg2 / dy;
	        b2 += db2 / dy;
	        
       drawLine(buff, new Point2D((int) curx1, scanlineY, new ColorType(r1, g1, b1)),new Point2D
	           ((int) curx2, scanlineY, new ColorType(r2, g2, b2))); // use drawline function. two points: current x1 and current x2
	       curx1 += slope1; // update current x1
	       curx2 += slope2; // update current x2
       
     }
   }

   public static void fillTopTriangle(BufferedImage buff, Point2D v1, Point2D v2, Point2D v3) {
	     
	   float slope1 = (float) (v3.x - v1.x) / (v3.y - v1.y);
	     float slope2 = (float) (v3.x - v2.x) / (v3.y - v2.y);
	     float curx1 = v3.x; // current x1 and x2 using v3 because filltop 
	     float curx2 = v3.x;
	
	     // color change
	     float dr1 = (v1.c.r - v3.c.r), dg1 = (v1.c.g - v3.c.g), db1 = (v1.c.b - v3.c.b);
	     float dr2 = (v2.c.r - v3.c.r), dg2 = (v2.c.g - v3.c.g), db2 = (v2.c.b - v3.c.b);
	    
	     // initialize the first color
	     float r1 = v3.c.r, g1 = v3.c.g, b1 = v3.c.b;
	     float r2 = v3.c.r, g2 = v3.c.g, b2 = v3.c.b;
	
	     float dy = v3.y - v1.y;
     for (int scanlineY = v3.y; scanlineY > v2.y; scanlineY--) {
	    	r1 += dr1 / dy; // update the color 
	        g1 += dg1 / dy;
	        b1 += db1 / dy;
	        
	        r2 += dr2 / dy;
	        g2 += dg2 / dy;
	        b2 += db2 / dy;
	        
	       drawLine(buff, new Point2D((int) curx1, scanlineY, new ColorType(r1, g1, b1)),
	    		   new Point2D ((int) curx2, scanlineY, new ColorType(r2, g2, b2)));	
	       	curx1 -= slope1; 
	       	curx2 -= slope2;
       
     }
     
   }

 /////////////////////////////////////////////////
 // for texture mapping (Extra Credit for CS680)
 /////////////////////////////////////////////////
 public static void triangleTextureMap(BufferedImage buff, BufferedImage texture, Point2D p1, Point2D p2, Point2D p3)
 {
  // replace the following line with your implementation
  drawPoint(buff, p3);
 }
}